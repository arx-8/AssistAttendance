package com.example

import java.io.File

object Consts {
  // local path
  private val PATH_CURRENT_DIR = new File(".").getAbsoluteFile.getParent + "\\"
  val PATH_SETTINGS_FILE = PATH_CURRENT_DIR + "resources\\settings.json"
  val PATH_GOOGLE_DRIVE_CLIENT_SECRET = PATH_CURRENT_DIR + "resources\\secret\\" + "client_secret.json"
}
