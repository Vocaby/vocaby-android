package com.vocaby.application.core.util

import kotlinx.coroutines.yield

object VocabyAlgo {
    suspend fun binarySearchPrefix(list: List<String>, search: String): Int {
        var low = 0
        var high = list.size-1
        var index = -1

        while (search.isNotEmpty() && low <= high) {
            yield()
            val mid = (low + high) / 2

            if (list[mid] == search) {
                index = mid
                break
            }

            if (list[mid].startsWith(search)) {
                if (mid > 0 && list[mid - 1].startsWith(search)) {
                    high = mid - 1
                    continue
                } else {
                    index = mid
                    break
                }
            }

            if (list[mid] < search) {
                low = mid + 1
            } else if (list[mid] > search) {
                high = mid - 1
            }
        }

        return index
    }
}