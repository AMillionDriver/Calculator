package com.axoloth.calculator.by.sky.logic

/**
 * KMapLogic - Menggunakan algoritma Quine-McCluskey untuk penyederhanaan Boolean.
 * Mendukung hingga 4 variabel (A, B, C, D) dengan pencatatan langkah-langkah.
 */
object KMapLogic {

    data class Result(val expression: String, val steps: String)

    fun solveWithSteps(numVars: Int, minterms: List<Int>, dontCares: List<Int> = emptyList()): Result {
        if (minterms.isEmpty()) return Result("0", "Tidak ada minterm (F = 0)")
        if (minterms.size + dontCares.size == (1 shl numVars)) return Result("1", "Semua kotak terisi (F = 1)")

        val steps = StringBuilder()
        steps.append("Langkah 1: Mengelompokkan minterm berdasarkan jumlah bit '1'\n")
        
        val allTerms = (minterms + dontCares).distinct().sorted()
        var groups = mutableMapOf<Int, MutableList<String>>()

        for (term in allTerms) {
            val binary = term.toString(2).padStart(numVars, '0')
            val ones = binary.count { it == '1' }
            groups.getOrPut(ones) { mutableListOf() }.add(binary)
        }

        // Print initial groups
        groups.keys.sorted().forEach { k ->
            steps.append("Grup $k: ${groups[k]}\n")
        }

        val primeImplicants = mutableSetOf<String>()
        var currentGroups = groups
        var iteration = 1

        while (currentGroups.isNotEmpty()) {
            val nextGroups = mutableMapOf<Int, MutableList<String>>()
            val combined = mutableSetOf<String>()
            val keys = currentGroups.keys.sorted()
            
            steps.append("\nIterasi $iteration: Kombinasi grup\n")
            var foundCombination = false

            for (i in 0 until keys.size - 1) {
                val group1 = currentGroups[keys[i]]!!
                val group2 = currentGroups[keys[i + 1]]!!

                for (t1 in group1) {
                    for (t2 in group2) {
                        val diffPos = getDiffPosition(t1, t2)
                        if (diffPos != -1) {
                            val combinedTerm = t1.substring(0, diffPos) + "-" + t1.substring(diffPos + 1)
                            val ones = combinedTerm.count { it == '1' }
                            if (!nextGroups.getOrPut(ones) { mutableListOf() }.contains(combinedTerm)) {
                                nextGroups[ones]!!.add(combinedTerm)
                                steps.append("Gabungkan $t1 & $t2 -> $combinedTerm\n")
                                foundCombination = true
                            }
                            combined.add(t1)
                            combined.add(t2)
                        }
                    }
                }
            }

            for (group in currentGroups.values) {
                for (term in group) {
                    if (term !in combined) {
                        primeImplicants.add(term)
                    }
                }
            }
            
            if (!foundCombination) steps.append("Tidak ada lagi kombinasi yang dimungkinkan.\n")
            currentGroups = nextGroups
            iteration++
        }

        steps.append("\nPrime Implicants yang ditemukan:\n")
        primeImplicants.forEach { steps.append("- $it (${formatTerm(it, numVars)})\n") }

        val filteredImplicants = primeImplicants.filter { imp ->
            minterms.any { m -> matches(imp, m.toString(2).padStart(numVars, '0')) }
        }

        val finalExpr = formatExpression(filteredImplicants, numVars)
        steps.append("\nLangkah Akhir: Pemilihan Essential Prime Implicants\n")
        steps.append("Hasil Akhir: F = $finalExpr")

        return Result(finalExpr, steps.toString())
    }

    // Legacy support
    fun solve(numVars: Int, minterms: List<Int>, dontCares: List<Int> = emptyList()): String {
        return solveWithSteps(numVars, minterms, dontCares).expression
    }

    private fun getDiffPosition(s1: String, s2: String): Int {
        var diffCount = 0
        var pos = -1
        for (i in s1.indices) {
            if (s1[i] != s2[i]) {
                diffCount++
                pos = i
            }
        }
        return if (diffCount == 1) pos else -1
    }

    private fun matches(implicant: String, minterm: String): Boolean {
        for (i in implicant.indices) {
            if (implicant[i] != '-' && implicant[i] != minterm[i]) return false
        }
        return true
    }

    private fun formatTerm(imp: String, numVars: Int): String {
        val varNames = listOf("A", "B", "C", "D")
        val term = StringBuilder()
        for (i in imp.indices) {
            if (imp[i] == '1') term.append(varNames[i])
            else if (imp[i] == '0') term.append(varNames[i]).append("'")
        }
        return if (term.isEmpty()) "1" else term.toString()
    }

    private fun formatExpression(implicants: List<String>, numVars: Int): String {
        if (implicants.isEmpty()) return "0"
        return implicants.joinToString(" + ") { formatTerm(it, numVars) }
    }
}
