import java.io.File

object Consts {
  // local path
  private val PATH_CURRENT_DIR = new File(".").getAbsoluteFile.getParent + "\\"
  val PATH_SETTINGS_FILE = PATH_CURRENT_DIR + "settings.json"
}
