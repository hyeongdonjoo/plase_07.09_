package com.example.myapplication2

/**
 * Firestore 메뉴 문서 구조가
 * name, desc, category가 단일 String이든, 언어별 Map<String, String>이든
 * 모두 대응할 수 있도록 설계한 MenuItem 클래스입니다.
 */
data class MenuItem(
    // 단일 언어(기본값: 한국어)로만 쓸 경우
    val name: String = "",
    val desc: String = "",
    val category: String = "",

    // 다국어 map 구조로 Firestore에 저장된 경우 대응
    val nameMap: Map<String, String> = mapOf(),
    val descMap: Map<String, String> = mapOf(),
    val categoryMap: Map<String, String> = mapOf(),

    val price: Int = 0,
    val image: String = "",   // 이미지 ID 또는 URL
    var quantity: Int = 1
) {
    /**
     * 현재 언어코드에 맞는 name 반환 (없으면 한글, 영어 순)
     */
    fun getDisplayName(lang: String): String =
        nameMap[lang] ?: nameMap["ko"] ?: nameMap["en"] ?: name

    /**
     * 현재 언어코드에 맞는 desc 반환 (없으면 한글, 영어 순)
     */
    fun getDisplayDesc(lang: String): String =
        descMap[lang] ?: descMap["ko"] ?: descMap["en"] ?: desc

    /**
     * 현재 언어코드에 맞는 category 반환 (없으면 한글, 영어 순)
     */
    fun getDisplayCategory(lang: String): String =
        categoryMap[lang] ?: categoryMap["ko"] ?: categoryMap["en"] ?: category
}
