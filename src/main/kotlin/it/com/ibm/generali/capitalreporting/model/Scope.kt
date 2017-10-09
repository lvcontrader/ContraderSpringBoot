package it.com.ibm.generali.capitalreporting.model

import java.io.Serializable
import javax.persistence.*

@Entity
open class Scope : Serializable
{
    companion object
    {
        val TAG_DELIMITER = ';'
    }

    @Id
    @TableGenerator(name = "TABLE_GEN", table = "T_GENERATOR", pkColumnName = "GEN_KEY", pkColumnValue = "OUTPUT_DEF", valueColumnName = "GEN_VALUE", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    var id: Long = 0L
        private set

    lateinit var name: String
    var tags: String = ""
    var published: Boolean = false

    var parent: Long = -1L

    @OneToMany(mappedBy = "scope", cascade = arrayOf(CascadeType.ALL))
    var reports: MutableSet<Report> = mutableSetOf()

    fun addReport(report: Report)
    {
        this.reports.add(report)
    }

    fun hasNoReports(): Boolean =
            this.reports.size == 0

    fun setAllTags(tags: List<String>)
    {
        var allTags = ""
        tags.forEach { tag -> allTags += tag + Scope.TAG_DELIMITER }
        this.tags = allTags.substring(0, allTags.length - 1)
    }

    fun getAllTags(): List<String>
    {
        return this.tags.split(Scope.TAG_DELIMITER)
    }

}