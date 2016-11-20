package com.example.util

import java.time.LocalDateTime

/**
  * 勤怠報告用の日時を扱うクラス
  */
class ReportDateTimeUtils(dt: LocalDateTime) {
  val CLOSING_DAY_OF_MONTH = 20

  /**
    * 補正した時間を返す
    * <pre>
    * (e.g.)
    * 8:41 → 9:00
    * 9:09 → 9:00
    * 14:11 → 14:30
    * </pre>
    *
    * @return
    */
  def getTimeOfJustToCorrect: String = {
    val pairHM = dt.getMinute match {
      case min if (0 <= min && min <= 10) => (dt.getHour, 0)
      case min if (10 < min && min < 40) => (dt.getHour, 30)
      case _ => (dt.getHour + 1, 0)
    }
    pairHM._1 + ":" + "%02d".format(pairHM._2)
  }

  /**
    * 月度を返す(1～12)
    * 20日締め
    * e.g.
    * 11/20 → 11月度
    * 11/21 → 12月度
    *
    * @return
    */
  def getThisFiscalMonth: Int = {
    if (CLOSING_DAY_OF_MONTH < dt.getDayOfMonth) {
      return dt.getMonthValue + 1
    }
    dt.getMonthValue
  }

  /**
    * 月度の始めを基準にした日数を返す
    * (e.g. 21日が月度開始日の場合)
    *
    * @return
    */
  def getDaysOfDiffFromBase: Int = {
    // TODO
    0
  }

  /**
    * 今月度のシート名を、現在日付から導出して返す
    * e.g. 2016_11
    */
  def getThisFiscalMonthsSheetName: String = {
    dt.getYear.toString + "_" + "%02d".format(dt.getMonthValue)
  }
}
