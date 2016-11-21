package com.example.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import com.example.Consts

/**
  * 勤怠報告用の日時を扱うクラス
  */
class ReportDateTimeUtils(dt: LocalDateTime) {

  /**
    * ちょうどよく補正した時間を返す
    * <pre>
    * (e.g.)
    * 8:41 → 9:00
    * 9:09 → 9:00
    * 14:11 → 14:30
    * </pre>
    *
    * @return
    */
  def getTimeOfJust: String = {
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
    * <pre>
    * e.g.
    * 11/20 → 11月度
    * 11/21 → 12月度
    * </pre>
    *
    * @return
    */
  def getThisFiscalMonth: Int = {
    if (Consts.CLOSING_DAY_OF_MONTH < dt.getDayOfMonth) {
      return dt.getMonthValue + 1
    }
    dt.getMonthValue
  }

  /**
    * 月度の始めを基準にした日数を返す
    * 20日締め、21日が月度開始日になる
    * <pre>
    * e.g. (10月度の場合)
    * 10/21 -> 0
    * 11/01 -> 11
    * 11/20 -> 30
    * </pre>
    *
    * @return
    */
  def getDayOfFiscalMonth: Int = {
    // 「締め日～末日」なら、その日数分引いた数が月度の日数である
    val dayOfMon = dt.getDayOfMonth
    if (Consts.CLOSING_DAY_OF_MONTH < dayOfMon) {
      return dayOfMon - Consts.CLOSING_DAY_OF_MONTH - 1
    }

    // 月度の最初の日を取得 -> get差分
    val baseDt = dt.minusMonths(1).withDayOfMonth(Consts.CLOSING_DAY_OF_MONTH + 1)
    ChronoUnit.DAYS.between(baseDt, dt).toInt
  }

  /**
    * 今月度のシート名を、現在日付から導出して返す
    * e.g. 2016_11
    */
  def getThisFiscalMonthsSheetName: String = {
    dt.getYear.toString + "_" + "%02d".format(getThisFiscalMonth)
  }
}
