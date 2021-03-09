import com.elbekD.bot.types.InlineKeyboardMarkup
import io.github.bonigarcia.wdm.WebDriverManager
import org.apache.commons.io.FileUtils
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File

abstract class Test(userId: Int) {
    protected val question = File("./question$userId.png")
    abstract val questionCount: Int
    val driver: WebDriver

    init {
        WebDriverManager.chromedriver().setup()
//        driver = ChromeDriver()
        val options = ChromeOptions().addArguments("--headless")
        driver = ChromeDriver(options)

    }


    protected val js: JavascriptExecutor = driver as JavascriptExecutor

    abstract fun processAnswer(data: String)
    abstract fun getResult(id: Int)

    abstract fun takeQuestion(): File

    abstract fun getKeyBoard(id: Int): InlineKeyboardMarkup

    abstract fun prepare()

    fun getPage(page: String) {
        driver.get(page)
    }

    fun takeScrElement(id: String) {
        val element = driver.findElement(By.id(id))
        val scrFile: File = element.getScreenshotAs(OutputType.FILE)
        FileUtils.copyFile(scrFile, question)
    }

    fun clickOnElement(id: String, driver: WebDriver) {
        val element = driver.findElement(By.id(id))
        element.click()
    }
}