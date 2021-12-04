package com.vocaby.app.utils

object VocabyAlgo {
    fun binarySearchPrefix(list: List<String>, search: String): Int {
        return binarySearchPrefix(0, list.size - 1, search, list)
    }

    private fun binarySearchPrefix(start: Int, end: Int, search: String, list: List<String>): Int {
        if (start <= end) {
            val mid = (start + end) / 2
            val midEntry = list[mid]
            val compResult = search.compareTo(midEntry)

            if (compResult == 0) {
                return mid
            }

            if (midEntry.startsWith(search)) {
                if (mid - 1 == -1) {
                    return 0
                } else if (mid - 1 > -1) {
                    return if (list[mid - 1].startsWith(search)) {
                        binarySearchPrefix(start, mid - 1, search, list)
                    } else {
                        mid
                    }
                }
            }

            return if (compResult < 0) {
                binarySearchPrefix(start, mid - 1, search, list)
            } else {
                binarySearchPrefix(mid + 1, end, search, list)
            }
        }

        return -1
    }
}