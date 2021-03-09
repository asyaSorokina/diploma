import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import java.io.File


class LusherTest(userId: Int) : Test(userId) {


    private var cards: ArrayList<WebElement> = ArrayList()
    override var questionCount = 0

    companion object {
        val result: File = File("./LusherResults.txt")
    }

    override fun takeQuestion(): File {
        getCards()
        val cardsId = cards.map { it.getAttribute("id") }
        cardsId.forEachIndexed { i, cardId -> executeScript("${i + 1}", "55%", cardId) }
        takeScrElement("pageContent")
        questionCount++
        return question
    }

    override fun prepare() {
        getPage("https://psytests.org/luscher/fullcolor-run.html")
        clickOnElement("frmRunButton", driver)
    }

    override fun processAnswer(data: String) {
        val id = data.toInt()
        cards[id].click()
        if (cards.size > 2) {
            executeScript("", "0", cards[id].getAttribute("id"))
        }
        cards.removeAt(id)
    }

    override fun getResult(id: Int) {
        synchronized(result) {
            result.appendText(
                driver.findElement(By.xpath("//*[@id=\"pageContent\"]/div[1]/table/tbody/tr[3]/td[2]/a"))
                    .getAttribute("href")
            )
            result.appendText("  $id")
            result.appendText("\n")
            driver.quit()
        }
    }

    private fun executeScript(value: String, paddingValue: String, id: String) {
        js.executeScript(
            """const card = document.getElementById("$id");
            |card.innerHTML = "$value"; 
            |card.style.paddingTop = "$paddingValue";
            |card.style.paddingBottom = "$paddingValue";
        """.trimMargin()
        )
    }

    override fun getKeyBoard(id: Int): InlineKeyboardMarkup {
        val rowAmount: Int = if (cards.size < 5) 1 else 2
        val rows = ArrayList<ArrayList<InlineKeyboardButton>>()
        for (i in 0..rowAmount) rows.add(ArrayList())
        cards.forEachIndexed { i, _ ->
            if (i < 4) rows[0].add(
                InlineKeyboardButton(
                    "${i + 1}",
                    callback_data = "$i $id"
                )
            ) else rows[1].add(InlineKeyboardButton("${i + 1}", callback_data = "$i $id"))
        }
        return InlineKeyboardMarkup(rows)
    }

    private fun getCards() {
        if (cards.size <= 1) {
            cards = driver.findElements(By.xpath("//*[contains(@id,'card')]\n")) as ArrayList<WebElement>
        }
    }
}

//*[@id="lsDeck"]/div[1]/div[2]
//*[@id="lsDeck"]/div[1]/div[2]