package com.axoloth.calculator.by.sky.logic

/**
 * KMapLogic - Menggunakan algoritma Quine-McCluskey untuk penyederhanaan Boolean.
 * Mendukung hingga 4 variabel (A, B, C, D) dengan pencatatan langkah-langkah.
 */
object KMapLogic {

    data class Result(
        val expression: String,
        val steps: String,
        val truthTable: List<Pair<Int, Boolean>>,
        val variableNames: List<Char>
    )

    sealed class LogicNode {
        data class Variable(val name: Char) : LogicNode()
        data class NotGate(val input: LogicNode) : LogicNode()
        data class AndGate(val inputs: List<LogicNode>) : LogicNode()
        data class OrGate(val inputs: List<LogicNode>) : LogicNode()
    }

    fun solveWithSteps(numVars: Int, minterms: List<Int>, dontCares: List<Int> = emptyList()): Result {
        val varNames = listOf('A', 'B', 'C', 'D').take(numVars)
        if (minterms.isEmpty()) return Result("0", "Tidak ada minterm (F = 0)", generateTruthTable(numVars, emptyList()), varNames)
        if (minterms.size + dontCares.size == (1 shl numVars)) return Result("1", "Semua kotak terisi (F = 1)", generateTruthTable(numVars, (0 until (1 shl numVars)).toList()), varNames)

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

        return Result(finalExpr, steps.toString(), generateTruthTable(numVars, minterms), varNames)
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

    private fun generateTruthTable(numVars: Int, minterms: List<Int>): List<Pair<Int, Boolean>> {
        val totalRows = 1 shl numVars
        return (0 until totalRows).map { i ->
            i to minterms.contains(i)
        }
    }

    fun getVariableNames(expression: String): List<Char> {
        val vars = expression.filter { it.isLetter() }.uppercase().toSet().toList().sorted()
        return if (vars.isEmpty()) listOf('A', 'B', 'C', 'D') else vars
    }

    fun parseSOP(expression: String): LogicNode? {
        if (expression == "0") return null
        if (expression == "1") return null

        val terms = expression.split("+").map { it.trim() }.filter { it.isNotEmpty() }
        val orInputs = mutableListOf<LogicNode>()

        for (term in terms) {
            val andInputs = mutableListOf<LogicNode>()
            var i = 0
            while (i < term.length) {
                val c = term[i].uppercaseChar()
                if (c in 'A'..'Z') {
                    var node: LogicNode = LogicNode.Variable(c)
                    if (i + 1 < term.length && term[i + 1] == '\'') {
                        node = LogicNode.NotGate(node)
                        i++
                    }
                    andInputs.add(node)
                }
                i++
            }
            if (andInputs.size == 1) {
                orInputs.add(andInputs[0])
            } else if (andInputs.size > 1) {
                orInputs.add(LogicNode.AndGate(andInputs))
            }
        }

        return if (orInputs.size == 1) orInputs[0] else if (orInputs.size > 1) LogicNode.OrGate(orInputs) else null
    }

    fun evaluate(node: LogicNode, inputs: Map<Char, Boolean>): Boolean {
        return when (node) {
            is LogicNode.Variable -> inputs[node.name] ?: false
            is LogicNode.NotGate -> !evaluate(node.input, inputs)
            is LogicNode.AndGate -> node.inputs.all { evaluate(it, inputs) }
            is LogicNode.OrGate -> node.inputs.any { evaluate(it, inputs) }
        }
    }

    fun generateTruthTableForExpression(expression: String): Pair<List<Char>, List<Pair<Int, Boolean>>> {
        val vars = getVariableNames(expression)
        val node = parseSOP(expression) ?: return vars to emptyList<Pair<Int, Boolean>>()
        val numVars = vars.size
        val totalRows = 1 shl numVars
        val table = (0 until totalRows).map { i ->
            val inputs = mutableMapOf<Char, Boolean>()
            for (j in 0 until numVars) {
                inputs[vars[j]] = (i shr (numVars - 1 - j)) and 1 == 1
            }
            i to evaluate(node, inputs)
        }
        return vars to table
    }
}
