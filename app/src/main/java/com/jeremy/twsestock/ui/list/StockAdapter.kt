package com.jeremy.twsestock.ui.list

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jeremy.twsestock.R
import com.jeremy.twsestock.data.model.PriceTrend
import com.jeremy.twsestock.databinding.ItemStockBinding
import com.jeremy.twsestock.ui.common.resolveColor
import com.jeremy.twsestock.ui.common.toChangeText
import com.jeremy.twsestock.ui.common.toCountText
import com.jeremy.twsestock.ui.common.toHundredMillionText
import com.jeremy.twsestock.ui.common.toLotText
import com.jeremy.twsestock.ui.common.toPriceText

class StockAdapter : ListAdapter<StockUiModel, StockAdapter.StockViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StockViewHolder(private val binding: ItemStockBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StockUiModel) = with(binding) {
            val closingColor = item.closingTrend.resolveColor(root)
            val changeColor = item.changeTrend.resolveColor(root)

            tvCode.text = item.code
            tvName.text = item.name

            tvClosingPrice.apply {
                text = item.closingPrice.toPriceText()
                setTextColor(closingColor)
            }
            tvChange.apply {
                text = item.change.toChangeText(item.changePercent)
                setTextColor(changeColor)
            }

            tvMonthlyAveragePrice.text = item.monthlyAveragePrice.toPriceText()
            tvMonthlyAverageTag.apply {
                setText(item.closingTrend.tagRes())
                backgroundTintList = ColorStateList.valueOf(closingColor)
            }

            tvOpeningPrice.text = item.openingPrice.toPriceText()
            tvHighestPrice.text = item.highestPrice.toPriceText()
            tvLowestPrice.text = item.lowestPrice.toPriceText()
            tvTradeVolume.text = item.tradeVolume.toLotText()
            tvTransactionCount.text = root.context.getString(R.string.format_transaction_count, item.transactionCount.toCountText())
            tvTradeValue.text = root.context.getString(R.string.format_trade_value, item.tradeValue.toHundredMillionText())
        }

        @StringRes
        private fun PriceTrend.tagRes(): Int = when (this) {
            PriceTrend.UP -> R.string.tag_above_monthly_average
            PriceTrend.DOWN -> R.string.tag_below_monthly_average
            PriceTrend.FLAT -> R.string.tag_equal_monthly_average
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StockUiModel>() {
        override fun areItemsTheSame(oldItem: StockUiModel, newItem: StockUiModel) =
            oldItem.code == newItem.code

        override fun areContentsTheSame(oldItem: StockUiModel, newItem: StockUiModel) =
            oldItem == newItem
    }
}
