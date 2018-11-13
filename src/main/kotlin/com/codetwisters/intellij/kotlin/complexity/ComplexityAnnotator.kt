package com.codetwisters.intellij.kotlin.complexity

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*


class ComplexityAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is KtElement) {
            KtPsiUtil.visitChildren(element, namedFunctionVisitor {
                val complexity = calculateComplexity(it.bodyExpression as KtBlockExpression)
                if (complexity > MAX_COMPLEXITY) {
                    val range = TextRange(
                            it.nameIdentifier?.textRange?.startOffset ?: 0,
                            it.nameIdentifier?.textRange?.endOffset ?: 0)

                    holder.createErrorAnnotation(range, "Method complexity is higher than it should be: $complexity > $MAX_COMPLEXITY")
                }
            }, null)
        }
    }

    private fun calculateComplexity(ktBlockExpression: KtBlockExpression): Int {
        var complexity = 1
        KtPsiUtil.visitChildren(ktBlockExpression,
                expressionRecursiveVisitor { ktExpression ->
                    when (ktExpression) {
                        is KtIfExpression,
                        is KtForExpression,
                        is KtTryExpression,
                        is KtWhileExpression -> complexity++

                        is KtWhenExpression -> {
                            complexity += ktExpression.entries.size
                        }
                    }
                },null)
        return complexity
    }

    companion object {
        const val MAX_COMPLEXITY = 10
    }
}
