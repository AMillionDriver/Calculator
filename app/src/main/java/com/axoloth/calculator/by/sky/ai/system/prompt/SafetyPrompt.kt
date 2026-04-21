package com.axoloth.calculator.by.sky.ai.system.prompt

object SafetyPrompt {
    val BANNED_KEYWORDS = listOf(
        "jailbreak", "ignore previous instructions", "system prompt",
        "developer mode", "DAN mode", "bypass", "unfiltered",
        "hack", "exploit", "root access", "acting as", "pretend to be", "hypothetical",
        "virtual machine", "terminal", "linux console", "no constraints", "unrestricted", "without limits",
        "output the full text above", "start from the beginning","raw markdown", "source code of this chat",
        "internal logic", "configuration settings","sql injection", "xss", "cross-site scripting",
        "payload", "brute force", "backdoor", "credential harvesting", "phishing script",
        "repeat back", "summarize your instructions", "start with 'you are'", "words above",
        "internal documentation", "system config", "reveal secret", "base64", "hexadecimal", "binary code",
        "translate to", "rot13", "morse code", "leetspeak", "reversed text", "unrestricted mode", "developer access granted",
        "ignore safety", "override policy", "sudo", "terminal command", "anonymous", "god mode", "reverse engineering", "decompile", "bypass subscription", "crack",
        "api key", "token", "database structure"
    )

    fun isSafe(input: String): Boolean {
        val lowerInput = input.lowercase()
        return BANNED_KEYWORDS.none { lowerInput.contains(it) }
    }

    const val REJECTION_MESSAGE = "Apasih Sok Asik Banget !"
}
