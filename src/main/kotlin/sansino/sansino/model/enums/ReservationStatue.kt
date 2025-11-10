package sansino.sansino.model.enums

enum class ReservationStatue {
    PENDING,       // رزرو ثبت شده ولی هنوز تایید نشده
    CONFIRMED,     // رزرو تایید شده و آماده استفاده
    CONFIRMEDOFFLINE,     // رزرو به صورت حضوری تایید و آماده استفاده
    CANCELLED,     // رزرو کنسل شده توسط کاربر یا مدیریت
    COMPLETED,     // رزرو انجام شده (زمان رزرو گذشته)
    EXPIRED        // رزرو پرداخت نشده یا استفاده نشده و زمانش گذشته
}