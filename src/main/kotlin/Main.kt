import com.elbekD.bot.Bot


fun main() {
    val token = ""
    val username = "SorokinaTestBot"
    val bot = Bot.createPolling(username, token)
    bot.initHandlers()
    bot.start()

}