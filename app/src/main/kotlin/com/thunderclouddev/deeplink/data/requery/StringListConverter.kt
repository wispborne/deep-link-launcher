package com.thunderclouddev.deeplink.data.requery


import com.thunderclouddev.deeplink.utils.empty
import io.requery.Converter

/**
 * https://gist.github.com/shymek/e784c964704eca007ab6d6966238df70
 * @author Jakub Szymion
 * *
 * @since 01.06.2016
 */
class StringListConverter : Converter<MutableList<String>, String> {
    private val SEPARATOR = "\u00007"

    override fun getMappedType(): Class<MutableList<String>> {
        return (MutableList::class.java as Class<MutableList<String>>)
    }

    override fun getPersistedType() = String::class.java

    override fun getPersistedSize() = null

    override fun convertToMapped(type: Class<out MutableList<String>>, value: String?)
            = value?.split(SEPARATOR)?.toMutableList() ?: mutableListOf()

    override fun convertToPersisted(list: MutableList<String>?): String {
        return list?.fold(String.empty) { left, right -> left + SEPARATOR + right } ?: String.empty

//        if (list == null) {
//            return ""
//        }
//
//        val sb = StringBuilder()
//        val size = list.size
//
//        for ((index, item) in list.withIndex()) {
//            sb.append(item)
//
//            if (index < size) {
//                sb.append(SEPARATOR)
//            }
//        }
//
//        return sb.toString()
    }
}