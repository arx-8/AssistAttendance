package com.example.util

import java.time.LocalDateTime

/**
  * 勤怠報告用の日時を扱うクラス
  */
class ReportDateTimeUtils {
  private val now = LocalDateTime.now()

  /**
    * 補正した時間を返す
    * (e.g.)
    * 9:35 → 9:30
    * 9:15 → 9:30
    *
    * @return
    */
  def getTimeOfJustToCorrect: String = {
    // TODO
    ""
  }

  /**
    * 月度を返す(1～12)
    *
    * @return
    */
  def getThisFiscalMonth: Int = {
    // TODO
    0
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
}
