import java.nio.charset.StandardCharsets

import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods

import scala.io.Source

object Settings {
  private val jsonText = Source.fromFile(Consts.PATH_SETTINGS_FILE, StandardCharsets.UTF_8.toString)
    .getLines()
    // 標準じゃないけど、コメントアウト対応
    .filter(!_.trim.startsWith("//"))
    .mkString

  // Brings in default date formats etc.
  private implicit val formats = DefaultFormats
  private val values = JsonMethods.parse(jsonText).extract[Values]

  // for public props
  val slack: Slack = values.slack


  /**
    * for json extract
    */
  case class Values(slack: Slack)

  case class Slack(
      incomingWebHookURL: String,
      userName: String,
      postChName: String
  )

}
