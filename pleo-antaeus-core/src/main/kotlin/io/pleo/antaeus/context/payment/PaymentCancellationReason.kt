package io.pleo.antaeus.context.payment

enum class PaymentCancellationReason {

    INVALID_INVOICE_STATUS,
    ACCOUNT_BALANCE_ISSUE,
    CURRENCY_MISMATCH,
    CUSTOMER_NOT_FOUND,
    NETWORK_FAILED,
    UNKNOWN;

//    companion object {
//        @JvmStatic
//        fun of(throwable: Throwable): PaymentCancellationReason {
//            val find = values().find { type ->
//                val tt = type
//                throwable is type.throwable  }
//            return find ?: UNKNOWN
//        }
//    }


}