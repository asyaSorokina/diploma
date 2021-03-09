import State.*
import com.elbekD.bot.Bot
import com.elbekD.bot.feature.chain.chain
import com.elbekD.bot.feature.chain.jumpTo
import com.elbekD.bot.feature.chain.jumpToAndFire
import com.elbekD.bot.feature.chain.terminateChain
import com.elbekD.bot.types.Message
import org.openqa.selenium.By

fun Bot.initHandlers() {
    val idToUser: HashMap<Int, User> = HashMap()
    chain("/start") { msg ->
        sendMessage(
            msg.chat.id,
            """Привет! Тебе будут представлены три теста. Внимательно читай вопрос и отвечай честно, не тратя много времени на обдумывание
        |Если ты готов, напиши "да"""".trimMargin()
        )
    }
        .then("begin_point") { msg ->
            run {
                val from = msg.from!!.id
                val user = User()
                idToUser[from] = user
                if (msg.text.equals("да", ignoreCase = true)) {
                    jumpToAndFire("lusher_test", msg)
                } else {
                    sendMessage(msg.chat.id, """Нужно написать "да"""")
                    jumpTo("begin_point", msg)
                }
            }
        }
        .then("lusher_test") { msg ->
            run {
                val userId = msg.from!!.id
                val user = idToUser[userId]!!
                sendMessage(msg.chat.id, "Тест Люшера")
                val lusher = LusherTest(userId)
                changeState(lusher, user, LUSHER)
                sendQuestion(lusher, msg, user.generateId())
            }
        }
        .build()
    onCallbackQuery {
        val data = it.data!!.split(" ").first()
        val lastId = it.data!!.split(" ").last().toInt()
        val from = it.from.id
        val user = idToUser[from]!!
        if (lastId == user.lastMessageId) {
            val newId = user.generateId()
            val chatId = it.message!!.chat.id
            val msg = it.message!!
            deleteMessage(chatId, msg.message_id)
            val userState = user.state
            if (userState == LUSHER) {
                val lusher = user.currentTest
                lusher.processAnswer(data)
                consumeTest(lusher, from, chatId, user, msg, newId, "Тест Мини-СМИЛ", SMIL)
            } else {
                if (userState == SMIL) {
                    val smil = user.currentTest
                    smil.processAnswer(data)
                    if (smil.questionCount == 65)
                        smil.driver.findElement(By.xpath("//*[@id=\"frmResButton\"]")).click()
                    consumeTest(smil, from, chatId, user, msg, newId, "Тест Кеттела", KETTEL)
                } else {
                    val kettel = user.currentTest
                    kettel.processAnswer(data)
                    if (kettel.questionCount == 188)
                        kettel.driver.findElement(By.xpath("//*[@id=\"frmResButton\"]")).click()
                    when {
                        kettel.driver.currentUrl.contains("result") -> {
                            user.state = END
                            kettel.getResult(from)
                            sendMessage(chatId, "Спасибо за участие!")
                        }
                        kettel.driver.currentUrl == "https://psytests.org/cattell/16pfA-run.html" -> {
                            user.state = KETTELAGE
                            sendMessage(chatId, "Введите ваш возраст (от 16 лет)")
                        }
                        else -> {
                            sendQuestion(kettel, msg, newId)
                        }
                    }
                }
            }
        }
    }
    chain(
        label = "kettelAgeQusetion",
        predicate = { msg -> msg.text != null && idToUser[msg.from!!.id]?.state == KETTELAGE },
        action = { msg ->
            try {
                val age = msg.text!!.toInt()
                val user = idToUser[msg.from!!.id]!!
                (user.currentTest as KettelTest).age = age
                if (age < 16 || age > 99) {
                    sendMessage(msg.chat.id, "Возраст должен быть больше 15 и меньше 100 лет ¯\\_(ツ)_/¯")
                    terminateChain(msg.chat.id)
                } else {
                    jumpToAndFire("ageQuestion", msg)
                }
            } catch (e: NumberFormatException) {
                sendMessage(msg.chat.id, "Возраст должен состоять из цифр")
                terminateChain(msg.chat.id)
            }
        }
    ).then("ageQuestion") {
        val user = idToUser[it.from!!.id]!!
        (user.currentTest as KettelTest).processAge()
        user.state = KETTEL
        jumpToAndFire("mainQuestions", it)
    }.then("mainQuestions") {
        val user = idToUser[it.from!!.id]!!
        sendQuestion(user.currentTest, it, user.generateId())
    }.build()
}

fun Bot.consumeTest(
    currentTest: Test,
    from: Int,
    chatId: Long,
    user: User,
    msg: Message,
    newId: Int,
    text: String,
    newState: State
) {
    if (currentTest.driver.currentUrl!!.contains("result")) {
        currentTest.getResult(from)
        sendMessage(chatId, text)
        val newTest = if (newState == SMIL) SmilTest(from) else KettelTest(from)
        changeState(newTest, user, newState)
        sendQuestion(newTest, msg, newId)
    } else {
        sendQuestion(currentTest, msg, newId)
    }
}

fun Bot.sendQuestion(test: Test, msg: Message, id: Int) {
    val question = test.takeQuestion()
    sendPhoto(
        msg.chat.id,
        question,
        markup = test.getKeyBoard(id)
    )
}

fun changeState(test: Test, user: User, newState: State) {
    user.currentTest = test
    user.state = newState
    test.prepare()
}