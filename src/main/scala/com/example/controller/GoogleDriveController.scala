package com.example.controller

import java.io.{File, FileNotFoundException}
import java.time.LocalDateTime
import java.util

import com.example.dao.GoogleSpreadsheetsDao
import com.example.util.ReportDateTimeUtils
import com.example.{Consts, Settings}
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.model._
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}

import scala.util.control.Exception._
import scala.util.{Failure, Success}

object GoogleDriveController {
  private val JSON_FACTORY = JacksonFactory.getDefaultInstance.asInstanceOf[JsonFactory]
  private val SCOPES = util.Arrays.asList(SheetsScopes.SPREADSHEETS)
  private val HTTP_TRANSPORT = try {
    GoogleNetHttpTransport.newTrustedTransport().asInstanceOf[HttpTransport]
  } catch {
    case t: Throwable =>
      t.printStackTrace()
      sys.exit()
  }

  /**
    * TODO 公式のサンプルコードが動かないので、古い認証方法を使ってる。
    * http://qiita.com/rubytomato@github/items/11c401d581492d3c0361
    *
    * @return
    */
  private def authorize(): Credential = {
    val p12file = new File(Consts.PATH_GOOGLE_DRIVE_P12_KEY_FILE)
    if (!p12file.exists) {
      throw new FileNotFoundException("Failed to load : " + Consts.PATH_GOOGLE_DRIVE_P12_KEY_FILE)
    }

    val credential = new GoogleCredential.Builder()
      .setTransport(HTTP_TRANSPORT)
      .setJsonFactory(JSON_FACTORY)
      .setServiceAccountId(Settings.googleDrive.serviceAccountId)
      .setServiceAccountPrivateKeyFromP12File(p12file)
      .setServiceAccountScopes(SCOPES)
      .build()

    credential
  }

  private def getSheetsService: Sheets = {
    val credential = authorize()
    new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
      .setApplicationName(Consts.APPLICATION_NAME)
      .build()
  }

  /** 始業と就業時間の行列INDEX */
  private val COORD_COL_OF_START = 3
  private val COORD_COL_OF_END = 5
  private val COORD_ROW_OF_DAYS_HEAD = 4

  /**
    *
    * @param isStart true:出社 | false:退社
    * @return
    */
  def run(isStart: Boolean) = {
    val sheetDao = new GoogleSpreadsheetsDao(getSheetsService)
    val now = new ReportDateTimeUtils(LocalDateTime.now)

    // Validate
    // ・ちゃんと今月度のシート参照できてるか
    val fiscalMonthsInSheetRange = now.getThisFiscalMonthsSheetName + "!" + "F1"
    val maybeValues = sheetDao.readCells(fiscalMonthsInSheetRange)
    maybeValues match {
      case Some(values) =>
        // 範囲1セルgetなので、決め打ちで取得
        val fisMon = values.getValues.get(0).get(0).toString
        if (fisMon != now.getThisFiscalMonth.toString) {
          throw new AccessFailException("今月度のシートじゃないっぽい。デバッグ値：" + fisMon)
        }
      case None =>
        throw new AccessFailException("月度シートアクセスに失敗")
    }

    // 書き込み
    // 文字列書き込みで気持ち悪いけど、GAS側の時間計算は正しく通ってるのでとりあえずよし。だめならシリアル値導出にPOIのライブラリを使う？
    val writeCell = new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(now.getTimeOfJust))
    val writeCoord = new GridCoordinate()
      .setSheetId(getReportSheetId)
      .setRowIndex(COORD_ROW_OF_DAYS_HEAD + now.getDayOfFiscalMonth)
      .setColumnIndex(if (isStart) COORD_COL_OF_START else COORD_COL_OF_END)
    sheetDao.writeCell(writeCell, writeCoord)
  }


  def runExample() = {
    val service = getSheetsService

    // TODO フォーマットが月毎に差異が無いので、記入先の位置は計算で導出できそう

    // WRITE
    val values = new util.ArrayList[CellData]()
    // 文字列書き込みで気持ち悪いけど、GAS側の時間計算は正しく通ってるのでとりあえずよし。だめならシリアル値導出にPOIのライブラリを使う？
    values.add(new CellData().setUserEnteredValue(new ExtendedValue().setStringValue("18:30")))

    // 書き込み先座標 row=4, col=3 が20日の始業時間セル
    val writeCoord = new GridCoordinate()
      .setSheetId(getReportSheetId)
      .setRowIndex(4)
      .setColumnIndex(COORD_COL_OF_START)

    allCatch withTry {
      writeCell(service, values, writeCoord)
    } match {
      case Success(resp) =>
        println(resp)

      case Failure(t) => t.printStackTrace()
    }
    println("end")
  }

  private def writeCell(service: Sheets, values: util.List[CellData], coord: GridCoordinate) = {
    val requests = new util.ArrayList[Request]()
    requests.add(new Request()
      .setUpdateCells(
        new UpdateCellsRequest()
          .setStart(coord)
          .setRows(util.Arrays.asList(new RowData().setValues(values)))
          .setFields("userEnteredValue,userEnteredFormat.backgroundColor")
      )
    )

    val batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests)
    service.spreadsheets().batchUpdate(Settings.googleDrive.spreadsheetId, batchUpdateRequest).execute()
  }

  /**
    * https://docs.google.com/spreadsheets/d/{SPREADSHEET_ID}/edit#gid={ここがSheetId！}
    * TODO 現状一定みたいだけど、不定なわけがないので、動的に取得しないと
    *
    * @return
    */
  private def getReportSheetId: Int = 6

  private def readCellsExample(service: Sheets) = {
    val sheetName = "2016_11" + "!"
    val range = sheetName + "B5:F35"
    allCatch withTry {
      service.spreadsheets().values().get(Settings.googleDrive.spreadsheetId, range).execute()
    } match {
      case Success(resp) =>
        val values = resp.values()
        println(values)

      case Failure(t) => t.printStackTrace()
    }
  }

  /**
    * TODO なんかもっと良い感じで
    */
  class AccessFailException(message: String = null) extends Exception(message) {}

}
