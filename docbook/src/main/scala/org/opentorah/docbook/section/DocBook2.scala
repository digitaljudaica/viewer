package org.opentorah.docbook.section

import java.io.File
import org.opentorah.xml.Xml
import org.opentorah.util.Collections
import Section.Parameters

trait DocBook2 extends Section {

  def defaultVariant: Variant = Variant(this, None)

  def usesIntermediate: Boolean = false

  protected def outputFileExtension: String

  protected def intermediateFileExtension: String = outputFileExtension

  // From general to specific
  final def parameterSections: List[Section] =  commonSections :+ this
  def commonSections: List[CommonSection]

  // Sanity check
  if (commonSections.contains(Common) && commonSections.contains(HtmlCommon) &&
    (commonSections != List(Common, HtmlCommon)))
    throw new IllegalArgumentException(s"Wrong section order for $this: $commonSections")

  protected def stylesheetUriName: String

  def usesRootFile: Boolean

  final def mainStylesheet(
    paramsStylesheetName: String,
    stylesheetUriBase: String,
    customStylesheets: Seq[String],
    values: NonOverridableParameters
  ): String = {
    // xsl:param has the last value assigned to it, so customization must come last;
    // since it is imported (so as not to be overwritten), and import elements must come first,
    // a separate "-param" file is written with the "default" values for the parameters :)

    val customStylesheetsString: String = customStylesheets
      .map(customStylesheet =>  s"""  <xsl:import href="$customStylesheet"/>""")
      .mkString("\n")

    val nonOverridableParameters: String = DocBook2.parametersBySection(
      parameterSections.map { section: Section => section.name -> section.nonOverridableParameters(values) })

    s"""${Xml.header}
       |<!-- DO NOT EDIT! Generated by the DocBook plugin. -->
       |<xsl:stylesheet $xslWithVersion>
       |  <xsl:import href="$stylesheetUriBase/$stylesheetUriName.xsl"/>
       |  <xsl:import href="$paramsStylesheetName"/>
       |  <!-- Custom stylesheets -->
       |$customStylesheetsString
       |
       |  <!-- Non-overridable parameters -->
       |$nonOverridableParameters
       |${mainStylesheetBody(values)}
       |</xsl:stylesheet>
       |""".stripMargin
  }

  protected def mainStylesheetBody(values: NonOverridableParameters): String

  def paramsStylesheet(parameters: Seq[(String, Parameters)]): String = {
    val parametersStr: String = DocBook2.parametersBySection(parameters)
    s"""${Xml.header}
       |<!-- DO NOT EDIT! Generated by the DocBook plugin. -->
       |<xsl:stylesheet $xslWithVersion>
       |$parametersStr
       |</xsl:stylesheet>
       |""".stripMargin
  }

  def usesCss: Boolean

  final def rootFileNameWithExtension(inputFileName: String, isIntermediate: Boolean): String =
    rootFilename(inputFileName) + "." + (if (isIntermediate) intermediateFileExtension else outputFileExtension)

  final def rootFilename(inputFileName: String): String =
    outputFileNameOverride.getOrElse(inputFileName)

  protected def outputFileNameOverride: Option[String] = None

  def copyDestinationDirectoryName: Option[String] = None

  def isPdf: Boolean = false

  def postProcess(
    inputDirectory: File,
    outputFile: File
  ): Unit = {
  }
}

object DocBook2 {

  val all: List[DocBook2] = List(Html, Epub2, Epub3, Pdf, Html2)

  def find(name: String): Option[DocBook2] = all.find(_.name.equalsIgnoreCase(name))

  def forName(name: String): DocBook2 = find(name).getOrElse {
    throw new IllegalArgumentException(
      s"""Unsupported output format $name;
         |supported formats are: ${getNames(all)}
         |""".stripMargin
    )
  }

  def getNames(processors: List[DocBook2]): String =
    processors.map(docBook2 => "\"" + docBook2.name +"\"").mkString("[", ", ", "]")

  private def parametersBySection(parameters: Seq[(String, Parameters)]): String = {
    val result =
      for {
        (sectionName: String, sectionParameters: Parameters) <- Collections.pruneSequenceOfMaps(parameters)
        if sectionParameters.nonEmpty
      } yield s"  <!-- $sectionName -->\n" + toString(sectionParameters)

    result.mkString("\n")
  }

  private def toString(parameters: Parameters): String = {
    val result =
      for ((name: String, value: String) <- parameters) yield
        if (value.nonEmpty) s"""  <xsl:param name="$name">$value</xsl:param>"""
        else s"""  <xsl:param name="$name"/>"""

    result.mkString("\n")
  }
}
