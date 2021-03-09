import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.ReplyKeyboard
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import java.io.File


class KettelTest(userId: Int) : Test(userId) {
    private var answers: ArrayList<WebElement> = ArrayList()
    override var questionCount = 0
    var age: Int = 0

    companion object {
        val result: File = File("./KettelResults.txt")
    }

    override fun prepare() {
        driver.get("https://psytests.org/cattell/16pfA-run.html")
    }

    override fun takeQuestion(): File {
        if (driver.currentUrl == "https://psytests.org/cattell/16pfA-run.html") {
            takeScrElement("stdSexSelector")
            answers = driver.findElements(By.xpath("//*[contains(@class,'stdSexCalcBtn')]")) as ArrayList<WebElement>
            questionCount++
        } else {
            takeScrElement("qtlBlock$questionCount")
            answers = driver.findElements(By.xpath("//*[contains(@id,'qtlQ$questionCount')]")) as ArrayList<WebElement>
            questionCount++
        }
        return question
    }

    override fun getKeyBoard(id: Int): InlineKeyboardMarkup {
        val rowButton = ArrayList<InlineKeyboardButton>()
        if (driver.currentUrl == "https://psytests.org/cattell/16pfA-run.html") {
            rowButton.add(InlineKeyboardButton("М", callback_data = "0 $id"))
            rowButton.add(InlineKeyboardButton("Ж", callback_data = "1 $id"))
        } else {
            rowButton.add(InlineKeyboardButton("А", callback_data = "0 $id"))
            rowButton.add(InlineKeyboardButton("Б", callback_data = "1 $id"))
            rowButton.add(InlineKeyboardButton("В", callback_data = "2 $id"))
        }
        return InlineKeyboardMarkup(listOf(rowButton))
    }

    override fun processAnswer(data: String) {
        answers[data.toInt()].click()
    }

    fun processAge() {
        assert(age != 0)
        for (c in age.toString()) {
            val value = Character.getNumericValue(c)
            driver.findElement(By.xpath("//*[@id=\"stdAgeSelector\"]/div[2]/div[${if (value == 0) 4 else if (value < 4) value else if (value < 7) (value + 1) else value + 2}]"))
                .click()
        }
        driver.findElement(By.xpath("//*[@id=\"frmRunButton\"]")).click()
    }

    override fun getResult(id: Int) {
        synchronized(result) {
            result.appendText(
                driver.findElement(By.xpath("//*[@id=\"pageContent\"]/div[1]/table/tbody/tr[2]/td[2]/a"))
                    .getAttribute("href")
            )
            result.appendText("  $id")
            result.appendText("\n")
            driver.quit()
        }
    }
}