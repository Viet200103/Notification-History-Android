package com.notisaver.database

import androidx.room.util.appendPlaceholders
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.google.firebase.analytics.FirebaseAnalytics

class RoomQuerySupport {
    companion object {
        internal fun buildNotificationPageQuery(
            limit: Int, offset: Int,
            searchValue: String,
            sortMode: NotisaveDatabase.SortMode,
            packageHashcodeList: List<String>? = null
        ): SupportSQLiteQuery {
            val isDesc = when(sortMode) {
                NotisaveDatabase.SortMode.DESC -> true
                NotisaveDatabase.SortMode.ASC -> false
            }

            val wherePackage = buildPackageArgumentQuery(packageHashcodeList)

            val whereSearch = buildSearchNotificationQuery()

            val sql =
                "SELECT packageHashcode, sbnKeyHashcode, logId, title, contentText, timePost, COUNT(logId) as countOfGroup " +
                "FROM (" +
                    "SELECT O.packageHashcode, O.sbnKeyHashcode, logId, title, contentText, timePost " +
                    "FROM ONotification AS O, ( " +
                        "SELECT packageHashcode " +
                        "FROM ONotification " +
                        "WHERE ( " +
                            "$wherePackage " +
                            "$whereSearch " +
                        ") " +
                        "GROUP BY packageHashcode " +
                        "ORDER BY CASE " +
                            "WHEN ? THEN timePost * -1 " +
                            "ELSE timePost " +
                        "END " +
                        "LIMIT ? " +
                        "OFFSET ? " +
                    ") AS P " +
                    "WHERE " +
                        "O.packageHashcode = P.packageHashcode AND " +
                        "$whereSearch " +
                    "ORDER BY CASE " +
                        "WHEN ? THEN timePost * -1 " +
                        "ELSE timePost " +
                    "END " +
                ") GROUP BY sbnKeyHashcode"

            val argList = arrayListOf<Any>()

            if (packageHashcodeList != null) {
                argList.addAll(packageHashcodeList)
            }

            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(isDesc)
            argList.add(limit)
            argList.add(offset)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(isDesc)

            return SimpleSQLiteQuery(sql, argList.toArray())
        }


        internal fun buildNStatusBarGroupQuery(
            searchValue: String,
            sortMode: NotisaveDatabase.SortMode,
            packageHashcodeList: List<String>?
        ): SupportSQLiteQuery {

            val isDesc = when(sortMode) {
                NotisaveDatabase.SortMode.DESC -> true
                NotisaveDatabase.SortMode.ASC -> false
            }

            val wherePackage = buildPackageArgumentQuery(packageHashcodeList)

            val whereSearch = buildSearchNotificationQuery()

            val sql =
            "SELECT *, count(*) as nCount " +
            "FROM ONotification " +
            "WHERE " +
                "$wherePackage " +
                "$whereSearch " +
            "GROUP BY sbnKeyHashcode " +
            "HAVING MAX(timePost) " +
            "ORDER BY CASE " +
                "WHEN ? THEN timePost * -1 " +
                "ELSE timePost " +
            "END "


            val argList = arrayListOf<Any>()

            if (packageHashcodeList != null) {
                argList.addAll(packageHashcodeList)
            }
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(isDesc)

            return SimpleSQLiteQuery(
                query = sql,
                bindArgs = argList.toArray()
            )
        }

        internal fun buildNotificationQuery(
            searchValue: String,
            sortMode: NotisaveDatabase.SortMode,
            packageHashcodeList: List<String>?
        ): SupportSQLiteQuery {
            val isDesc = when(sortMode) {
                NotisaveDatabase.SortMode.DESC -> true
                NotisaveDatabase.SortMode.ASC -> false
            }

            val wherePackage = buildPackageArgumentQuery(packageHashcodeList)

            val whereSearch = buildSearchNotificationQuery()

            val sql =
                "SELECT * FROM ONotification " +
                "WHERE " +
                    "$wherePackage" +
                    "$whereSearch" +
                "ORDER BY CASE " +
                    "WHEN ? THEN timePost * -1 " +
                    "ELSE timePost " +
                "END "

            val argList = arrayListOf<Any>()

            if (packageHashcodeList != null) {
                argList.addAll(packageHashcodeList)
            }

            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(searchValue)
            argList.add(isDesc)

            return SimpleSQLiteQuery(
                sql, argList.toArray()
            )
        }

        internal fun buildDeleteNotificationWithId(): Array<String> {
            return arrayOf(
                "ONotification",
                "logId = ?"
            )
        }

        fun buildDeleteAppMeta(): Array<String> {
            return arrayOf("AppMetaData", "packageHashcode = ?")
        }

        fun buildDeleteAppInCategory(): Array<String> {
            return arrayOf("CategoryAppMetaDataCrossRef", "packageHashcode = ?")
        }


        private fun buildSearchNotificationQuery(): CharSequence {
            return "CASE " +
                        "WHEN length(?) = 0 THEN 1 " +
                        "ELSE ( " +
                            "(title LIKE '%' || ? || '%') OR " +
                            "(contentText LIKE '%' || ? || '%') OR " +
                            "(subText LIKE '%' || ? || '%') " +
                        ")" +
                    "END "
        }

        private fun buildPackageArgumentQuery(
            packageHashcodeList: List<String>?
        ): CharSequence {
            val wherePackage = StringBuilder()
            if (packageHashcodeList != null) {
                val inputSize = packageHashcodeList.size
                wherePackage.append("packageHashcode in (")
                appendPlaceholders(wherePackage, inputSize)
                wherePackage.append(") AND ")
            }

            return wherePackage
        }
    }
}