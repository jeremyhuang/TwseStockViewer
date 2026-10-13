package com.jeremy.twsestock.ui.info

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import com.jeremy.twsestock.ui.list.StockUiModel
import com.jeremy.twsestock.ui.theme.StockTheme
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import androidx.core.graphics.drawable.toDrawable

/**
 * 個股估值資訊彈窗，內容以 Compose 繪製（見 [StockInfoCard]）。
 *
 * 資料放在 arguments 裡，旋轉螢幕或 App 被系統回收後重建時，彈窗內容不會遺失。
 * 請使用 [newInstance] 建立。
 */
class StockInfoDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 標題、按鈕都由 Compose 畫，不需要系統對話框的標題列。
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                StockTheme {
                    StockInfoCard(
                        code = args.getString(ARG_CODE).orEmpty(),
                        name = args.getString(ARG_NAME).orEmpty(),
                        updatedAt = args.getSerializableOf<LocalDateTime>(ARG_UPDATED_AT),
                        peRatio = args.getSerializableOf<BigDecimal>(ARG_PE_RATIO),
                        dividendYield = args.getSerializableOf<BigDecimal>(ARG_DIVIDEND_YIELD),
                        pbRatio = args.getSerializableOf<BigDecimal>(ARG_PB_RATIO),
                        onConfirm = ::dismiss,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // 圓角與陰影由 Compose 的 Surface 負責，視窗本身改成透明、大小跟著內容。
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    /**
     * 從 arguments 讀出 [Serializable] 參數。
     *
     * @receiver 彈窗的 arguments。
     * @param key 參數名稱。
     * @return 對應的值；未提供時為 null。
     */
    private inline fun <reified T : Serializable> Bundle.getSerializableOf(key: String): T? =
        BundleCompat.getSerializable(this, key, T::class.java)

    companion object {
        /** 顯示彈窗時使用的 Fragment tag，用來避免重複開啟。 */
        const val TAG = "StockInfoDialog"

        private const val ARG_CODE = "code"
        private const val ARG_NAME = "name"
        private const val ARG_UPDATED_AT = "updated_at"
        private const val ARG_PE_RATIO = "pe_ratio"
        private const val ARG_DIVIDEND_YIELD = "dividend_yield"
        private const val ARG_PB_RATIO = "pb_ratio"

        /**
         * 建立顯示指定個股估值的彈窗。
         *
         * @param stock 被點選的個股。
         * @param updatedAt App 取得資料的時間。
         * @return 已帶入參數的彈窗。
         */
        fun newInstance(stock: StockUiModel, updatedAt: LocalDateTime?): StockInfoDialogFragment =
            StockInfoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CODE, stock.code)
                    putString(ARG_NAME, stock.name)
                    putSerializable(ARG_UPDATED_AT, updatedAt)
                    putSerializable(ARG_PE_RATIO, stock.peRatio)
                    putSerializable(ARG_DIVIDEND_YIELD, stock.dividendYield)
                    putSerializable(ARG_PB_RATIO, stock.pbRatio)
                }
            }
    }
}
