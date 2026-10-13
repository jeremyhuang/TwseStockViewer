package com.jeremy.twsestock.ui.list

import com.jeremy.twsestock.data.model.Stock

/**
 * 將資料層的 [Stock] 轉成列表卡片使用的 [StockUiModel]。
 *
 * 只挑出卡片與估值彈窗需要的欄位，並帶入已計算好的趨勢與漲跌幅；數值不做格式化。
 *
 * @receiver 資料層的個股資料。
 * @return 卡片顯示用資料。
 */
fun Stock.toUiModel(): StockUiModel = StockUiModel(
    code = code,
    name = name,
    closingPrice = closingPrice,
    closingTrend = closingVsMonthlyAverage,
    change = change,
    changePercent = changePercent,
    changeTrend = changeTrend,
    monthlyAveragePrice = monthlyAveragePrice,
    openingPrice = openingPrice,
    highestPrice = highestPrice,
    lowestPrice = lowestPrice,
    tradeVolume = tradeVolume,
    transactionCount = transactionCount,
    tradeValue = tradeValue,
    peRatio = peRatio,
    dividendYield = dividendYield,
    pbRatio = pbRatio,
)
