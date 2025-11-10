package sansino.sansino.model.enums

enum class CancelingStatus {
//    درخواست لغو ثبت شده ولی هنوز پردازش نشده
    PENDING,
//      درخواست لغو تایید شده و رزرو کنسل شده وقتی که پول به حسابش واریز شد
    APPROVED,
//    درخواست لغو رد شده
    REJECTED,
//    زمان لغو گذشته و دیگر امکان لغو وجود ندارد
    EXPIRED,

}