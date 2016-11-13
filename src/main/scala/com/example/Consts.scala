package com.example

import java.io.File

object Consts {
  val APPLICATION_NAME = "AssistAttendance"

  // local path
  private val PATH_CURRENT_DIR = new File(".").getAbsoluteFile.getParent + "\\"
  val PATH_SETTINGS_FILE = PATH_CURRENT_DIR + "resources\\settings.json"
  val PATH_GOOGLE_DRIVE_CLIENT_SECRET = PATH_CURRENT_DIR + "resources\\secret\\" + "client_secret.json"
  val PATH_GOOGLE_DRIVE_P12_KEY_FILE = PATH_CURRENT_DIR + "resources\\secret\\" + "key_file.p12"
}
