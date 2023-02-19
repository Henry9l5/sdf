object Dependency {
    object Kotlin {
        const val Version = "1.7.21"
    }

    object Paper {
        const val Version = "1.19.3"
        const val API = "1.19"
    }

    object Libraries {
        private const val monun = "io.github.monun"

        val Lib = arrayListOf(
            "${monun}:tap-api:4.8.0",
            "${monun}:kommand-api:3.0.0",
            "${monun}:heartbeat-coroutines:0.0.4",
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4"
        )

        val LibCore = arrayListOf(
            "${monun}:tap-core:4.8.0",
            "${monun}:kommand-core:3.0.0",
            "${monun}:heartbeat-coroutines:0.0.4"
        )
    }
}