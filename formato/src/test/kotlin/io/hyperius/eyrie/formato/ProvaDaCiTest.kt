package io.hyperius.eyrie.formato

import kotlin.test.Test
import kotlin.test.assertEquals

class ProvaDaCiTest {
    @Test
    fun testeQuebradoDeProposito() {
        assertEquals(2, 1 + 2, "teste quebrado de propósito para a prova da CI")
    }
}
