import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import java.io.File

class SmilTest(userId: Int) : Test(userId) {
    override var questionCount: Int = 0
    var answers: ArrayList<WebElement> = ArrayList()

    companion object {
        val result: File = File("./SmilResults.txt")
    }

    override fun prepare() {
        getPage("https://psytests.org/mmpi/minismil-run.html")
    }

    override fun takeQuestion(): File {
        if (driver.currentUrl == "https://psytests.org/mmpi/minismil-run.html") {
            takeScrElement("stdSexSelector")
            answers = driver.findElements(By.xpath("//*[contains(@class,'stdSexCalcBtn')]")) as ArrayList<WebElement>
            return question
        } else {
            questionCount++
            setFontSizeById(questionCount, "3em")
            takeScrElement("qtlTest")
            answers = driver.findElements(By.xpath("//*[contains(@id,'qtlQ$questionCount')]")) as ArrayList<WebElement>
        }
        return question
    }

    override fun getKeyBoard(id: Int): InlineKeyboardMarkup {
        val rowButton = ArrayList<InlineKeyboardButton>()
        if (driver.currentUrl == "https://psytests.org/mmpi/minismil-run.html") {
            rowButton.add(InlineKeyboardButton("М", callback_data = "0 $id"))
            rowButton.add(InlineKeyboardButton("Ж", callback_data = "1 $id"))
        } else {
            rowButton.add(InlineKeyboardButton("Верно", callback_data = "0 $id"))
            rowButton.add(InlineKeyboardButton("Неверно", callback_data = "1 $id"))
        }
        return InlineKeyboardMarkup(listOf(rowButton))
    }

    override fun processAnswer(data: String) {
        answers[data.toInt()].click()
        if (driver.currentUrl == "https://psytests.org/mmpi/minismil-run.html") {
            driver.findElement(By.id("frmRunButton")).click()
        }
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

    private fun setFontSizeById(elementId: Int, value: String) {
        js.executeScript("""document.getElementById("qtlQuestion$elementId").style.fontSize = "$value";""")
    }

}