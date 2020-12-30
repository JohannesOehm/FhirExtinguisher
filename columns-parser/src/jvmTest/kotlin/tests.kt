import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Tests {

    fun selfTest(columns: Array<Column>) {
        assertTrue(columns.contentEquals(parseColumns(stringifyColumns(columns))), "self-test")
    }

    @Test
    fun simpleItem() {
        val columns = parseColumns("id:Observation.id")
        assertEquals(Column("id", "Observation.id", null), columns[0])

        selfTest(columns)
    }

    @Test
    fun simpleItems() {
        val columns = parseColumns("name:Patient.name,gender:gender")
        assertEquals(Column("name", "Patient.name", null), columns[0])
        assertEquals(Column("gender", "gender", null), columns[1])

        selfTest(columns)
    }

    @Test
    fun itemsWithSimpleTypes() {
        val columns = parseColumns("name:Patient.name@singleton,gender:gender")
        assertEquals(Column("name", "Patient.name", Singleton), columns[0])
        assertEquals(Column("gender", "gender", null), columns[1])

        selfTest(columns)
    }


    @Test
    fun explodeLong() {
        val columns = parseColumns("name:Patient.name@explodeLong(given:this.given,family:family),gender:gender")
        assertEquals(
            Column(
                name = "name",
                type = ExplodeLong(
                    arrayOf(
                        Column("given", "this.given", null),
                        Column("family", "family", null)
                    )
                ),
                expression = "Patient.name"
            ), columns[0]
        )
        assertEquals(Column("gender", "gender", null), columns[1])

        selfTest(columns)
    }

    @Test
    fun explodeWide() {
        val columns =
            parseColumns("name:Patient.name@explodeWide(\$disc:\$this.use,given:\$this.given,family:family),gender:gender")
        assertEquals(
            Column(
                name = "name",
                type = ExplodeWide(
                    "\$this.use", arrayOf(
                        Column("given", "\$this.given", null),
                        Column("family", "family", null)
                    )
                ),
                expression = "Patient.name"
            ), columns[0]
        )
        assertEquals(Column("gender", "gender", null), columns[1])

        selfTest(columns)
    }

    @Test
    fun explodeWideWithParensInSubColumns() {
        val columns =
            parseColumns("name:Patient.name@explodeWide(\$disc:\$this.use,given:\$this.given.substr(0),family:family),gender:gender")
        assertEquals(
            Column(
                name = "name",
                type = ExplodeWide(
                    "\$this.use", arrayOf(
                        Column("given", "\$this.given.substr(0)", null),
                        Column("family", "family", null)
                    )
                ),
                expression = "Patient.name"
            ), columns[0]
        )
        assertEquals(Column("gender", "gender", null), columns[1])

        selfTest(columns)
    }

    @Test
    fun explodeWideWithParensInSubColumnsWithSpecialChars() {
        val columns =
            parseColumns("name:Patient.name@explodeWide(\$disc:\$this.use,given:\$this.given.substr(0\\,0),family:family),gender:gender")
        assertEquals(
            Column(
                name = "name",
                type = ExplodeWide(
                    "\$this.use", arrayOf(
                        Column("given", "\$this.given.substr(0,0)", null),
                        Column("family", "family", null)
                    )
                ),
                expression = "Patient.name"
            ), columns[0]
        )
        assertEquals(Column("gender", "gender", null), columns[1])

        selfTest(columns)
    }


    @Test
    fun join() {
        val columns = parseColumns("name:Patient.name@join(\" \"),gender:gender@join(\", \")")
        assertEquals(Column("name", "Patient.name", Join(" ")), columns[0])
        assertEquals(Column("gender", "gender", Join(", ")), columns[1])

        selfTest(columns)

    }


    @Test
    fun escaping() {
        val columns =
            parseColumns("""na\:me\\r:Patient.name.where(use="foo\@ba\\r\,")@join(" "),gender:gender@join(", ")""")
        assertEquals(Column("""na:me\r""", """Patient.name.where(use="foo@ba\r,")""", Join(" ")), columns[0])
        assertEquals(Column("gender", "gender", Join(", ")), columns[1])

        selfTest(columns)
    }


}