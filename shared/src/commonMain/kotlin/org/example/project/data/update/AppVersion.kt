package org.example.project.data.update

data class AppVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<AppVersion> {

    companion object {
        val ZERO = AppVersion(0, 0, 0)
        val CURRENT = AppVersion(1, 0, 0)

        fun parse(version: String): AppVersion {
            val cleaned = version.trimStart('v', 'V')
            val parts = cleaned.split(".").map { it.toIntOrNull() ?: 0 }
            return AppVersion(
                major = parts.getOrElse(0) { 0 },
                minor = parts.getOrElse(1) { 0 },
                patch = parts.getOrElse(2) { 0 }
            )
        }
    }

    override fun compareTo(other: AppVersion): Int {
        val cmpMajor = major.compareTo(other.major)
        if (cmpMajor != 0) return cmpMajor
        val cmpMinor = minor.compareTo(other.minor)
        if (cmpMinor != 0) return cmpMinor
        return patch.compareTo(other.patch)
    }

    override fun toString(): String = "$major.$minor.$patch"
}
