# DocBook plugin for Gradle #

## Motivation ##

There is an excellent [DocBook](http://www.docbook.org/) plugin
for [Maven](https://maven.apache.org/index.html):
[docbkx-tools](https://github.com/mimil/docbkx-tools).
For [Gradle](https://gradle.org/), I found some Groovy scripts floating around,
but no general-purpose plugins. This is my attempt at one, inspired by the ideas
pioneered by the Maven plugin.


## Summary ##

Plugin uses Saxon with DocBook XSLT stylesheets to process a DocBook document
(and its includes) into HTML, EPUB and PDF. For PDF, DocBook is first processed
into XSL-FO, which is post-processed by Apache FOP. For PDF, JEuclid FOP plugin
can be enabled to process MathML. Document name and the list of the output formats
are configurable.

For each output format, plugin uses an XSL file that includes the official DocBook
XSLT stylesheet for that format. Those files can be used to set XSL parameters for
the DocBook stylesheets or for more extensive customizations. They are a part of the
Gradle project's sources, and if absent, are generated by the plugin (as are FOP
configuration file, CSS stylesheet used for HTML and EPUB, and XML catalog). 

XSL parameters can also be configured in the `Gradle` build file, in which case they
apply to all the output formats.

Values configured as `substitutions` are available within the DocBook documents by
their names. This mechanism can be used to insert things like processing date or the
version of the document into the output.    

If a data generator class is configured, it's `main()` method will be executed with
a directory path as a parameter. References in DocBook documents that are prefixed
with `data:` are resolved to files in that directory.

Resolution of XSLT stylesheets, generated data and substitutions is guided by an XML catalog
that plugin generates; this catalog can be used with an XML editor to replicate the setup of
that plugin uses in an environment more suitable than a code editor or an IDE for authoring DocBook.      

Additional URI nicknames, entities and other customizations can be added to the catalog
once it is generated by the plugin.


## Credits ##

I want to thank:
- [@mimil](https://github.com/mimil) for the [Maven DocBook plugin](https://github.com/mimil/docbkx-tools);
- [Norman Walsh](https://nwalsh.com/) for his work on [DocBook](http://www.docbook.org/),
[XML Catalogs](http://xmlcatalogs.org/) and [XML Resolver](https://xmlresolver.org/);
- [Bob Stayton](http://www.sagehill.net/bobsbio.html) for
[XSLT stylesheets for DocBook](https://github.com/docbook/xslt10-stylesheets)
and a [book]((http://www.sagehill.net/docbookxsl/)) about them;
- [Michael Kay](https://github.com/michaelhkay) for [Saxon 6](http://saxon.sourceforge.net/saxon6.5.5/)
and [Saxon 9](https://www.saxonica.com/documentation/documentation.xml);
- [Apache FOP team](https://xmlgraphics.apache.org/fop/) for Apache FOP;
- [Max Berger](https://github.com/maxberger) for the original work on JEuclid and
its [FOP plugin](http://jeuclid.sourceforge.net/jeuclid-fop/);
- [Emmeran Seehuber](https://github.com/rototor) for [updating JEuclid](https://github.com/rototor/jeuclid).


## Applying to a Gradle project ##

Plugin is [published](https://plugins.gradle.org/plugin/org.podval.docbook-gradle-plugin)
on the Gradle Plugin Portal. To apply it to a Gradle project:

```groovy
plugins {
  id "org.podval.docbook-gradle-plugin" version "0.2.1"
}
```

For projects with code that is used to generate data for inclusion in the DocBook files,
DocBook plugin needs to be applied *after* the Scala/Java plugin - or explicit dependency needs
to be added by hand:

```groovy
prepareDocBook.dependsOn classes
```

If project does not contain any code nor applies any core Gradle plugins,
to get basic tasks like "clean" and "build":

```groovy
apply plugin: 'base'
``` 

Plugin adds to the project Gradle task `prepareDocBook` that writes configuration files,
substitutions DTD and XML catalog, generates data (if configured) and unpacks DocBook XSLT stylesheets
 
Plugin adds to the project Gradle task `processDocBook` that processes DocBook :)

To make use of the DocBook processing in a directory-name-independent way:
```groovy
  project.sync {
    from project.tasks.getByPath(':<subproject with DocBook sources>:processDocBook').outputDirectory
    into '<published directory>'
  }  
```

## Configuration ##

Plugin adds to the project an extension that can be configured using `docBook` closure: 

```groovy
docBook {
  documentName = "paper"                                    // defaults to "index"

  outputFormats = ["HTML", "PDF", "EPUB2", "EPUB3"]         // by default, all supported formats are generated
                                                            // can be overridden on the command line by defining
                                                            // `-PdocBook.outputFormats="EPUB3, HTML"`

  dataGeneratorClass = "org.sample.stuff.paper.Tables"      // by default, no data is generated 

  xslParameters = [
    "body.font.family": "DejaVu Sans",
    "body.font.master": "12"
  ]

  substitutions = [
    "version": project.version       
  ]

  epubEmbeddedFonts = [ "Liberation Sans" ]                 // embedded fonts should be OpenType or WOFF!

  isJEuclidEnabled = true                                   // MathML processing for PDF is disabled by default 
}
```

### xslParameters ###

Plugin itself sets certain XSL parameters:
- `"img.src.path"` is set to `"images/"`;
- `"html.stylesheet"` is set to "css/docBook.xsl" (it's unclear if this makes sense for EPUB);
- `"base.dir"` is set to the output directory of the XSLT transform;
- `"root.filename"` is set to the output file.

Those parameters are set by the plugin only if they are not set in the DocBook extension's
`xslParameters` property, and it's best to leave them alone :)  

Since plugin sets `img.src.path` XSLT parameter, images should be referenced in the DocBook
files *without* the `images/` prefix!


### substitutions ###

Values configured via `substitutions` can be accessed in the DocBook files as XML entities with
appropriate names. For instance, if a substitution value is defined for `"version"`, it can be
accessed like this: `... current version: &version; dated...`.

To enable this functionality, DocBook document has to have a Docbook DOCTYPE declaration, even though
DocBook 5.x doesn't really have a DTD :):

```xml
<!DOCTYPE article PUBLIC "-//OASIS//DTD DocBook XML V5.0//EN" "http://www.oasis-open.org/docbook/xml/5.0/dtd/docbook.dtd">
```
For root element other than `"article"`, DOCTYPE declaration needs to be adjusted accordingly.

Another way to access substitution values is using processing instructions, for instance:
`<?eval version ?>`. This way, `xslParameters` are also accessible, since restrictions on the
entity names do not apply to the processing instructions. Unlike entities, processing instructions
can only be used inside XML elements, not in attribute values.

Undefined entities break the transformation, while undefined processing instructions are ignored.
Processing instructions handling is internal to the plugin, while entities resolution is externalized
using generated substitutions DTD and XML catalog, making it possible to reproduce entities substitution
in an XML editor. 

Both substitutions and xslParameters are also accessible in the CSS files, for instance:  
```css
@font-face {
  font-family: "@body.font.family@";
  src: url("@body-font-family-url@");
}

body {
  font-family: "@body-font-family@", sans-serif;
}
```

## Oxygen ##

Plugin's setup should be reproducible in an XML editor like [Oxygen](https://www.oxygenxml.com/):
- run Gradle task `prepareDocBook`;
- add a project-specific XML catalog `src/main/xml/catalog.xml` in
  Options | Preferences | XML | XML Catalog (check `Project Options`, not `Global Options`);
- use customization files from `src/main/xsl` to configure transformation scenarios. 
- define `img.src.path` parameter as `../images`.

TODO do it in the customization files!
TODO is there a way to make images available to Oxygen during editing? 


## Fonts ##

Default FOP configuration created by the plugin causes FOP to auto-detect available fonts,
which are then cached by FOP to save time. After installing new fonts, FOP's font cache file
needs to be removed for them to be detected.

FOP can't use some popular fonts like Roboto and NotoSans, and logs an error
"coverage set class table not yet supported" during fonts auto-detection;
see https://issues.apache.org/jira/browse/FOP-2704 for more details.

Plugin adds a Gradle task `listFonts` that can be used to list all the fonts that *are* available to FOP.

Some of the fonts that work well enough and support Latin, Russian and Hebrew scripts
are DejaVu and Liberation.

Property `epubEmbeddedFonts` configures font families that should be embedded in EPUB files.


## Directory Layout ##

Overview of the directory layout used by the plugin:

```
   src/main/
     css/docBook.css
     docBook/<documentName>.xml
     fop/fop.xconf
     images/
     xsl/
       epub.xsl
       fo.xsl
       html.xsl
     xml/catalog.xml  

   build/substitutions.dtd

   build/docBook/
     epub/<documentName>.epub
     html/
       css/
       images/
       index.html
     pdf/<documentName>.pdf

   build/docBookXsl/

   build/docBookTmp/
     data/
     epub/
     fo/
```

### Sources ###

Sources (under `src/main`) contain:
- DocBook document to be processed - `docBook/<documentName>.xml`;
- additional DocBook sources included by the main document - in `docBook/`;
- images used in the DocBook files - in `images/`;
- CSS stylesheet that is used for HTML and EPUB - in `css/docBook.css`;
- additional CSS files imported by the main one - in `css/`  
- DocBook XSLT customizations (specific to the output format) - in `xsl/html.xsl` and the like;
- FOP configuration - in `fop/fop.xconf`;
- XML catalog - in `xml/catalog.xml`;

Plugin will create CSS stylesheet, XSL customizations, FOP configuration file and XML catalog if
they are not present.  


### Output ###

Final output of the plugin is deposited under `build/docBook`,
in a separate directory for each output format:
- chunked HTML - in `epub/html`
- PDF - in `pdf/<documentName>.pdf`;
- EPUB file - in `epub/<documentName>.epub`.

For HTML and EPUB, CSS stylesheets and images are included in the output.


### Build ###

Plugin unpacks official DocBook XSLT stylesheets under `build/docBookXsl/`. References
in the customization XSL files that are prefixed with `http://docbook.sourceforge.net/release/xsl-ns/current/`
are resolved to the local copies, suppressing retrieval of the stylesheets for each build.
Gradle will retrieve them once when resolving dependency added by the plugin - and cache the JAR;
unpacking after each `clean` is cheap.

Substitutions configured in the `Gradle` build file are written into `build/substitutions.dtd` and referenced
from the XML catalog.

Data generated by the data generator resides under `duild/data`. References to the generated data
encountered in the DocBook documents are resolved to files in that directory. 

For output formats that require post-processing or packing, intermediate results are deposited
under `build/docBookTmp`.


## XSLT 2.0 ##

XSLT 1.0 DocBook stylesheets rely on multi-output extension of Saxon for chunking HTML.
This extension is not supported in Saxon after version 6.5.3 (new Saxon).
Switching to [XSLT 2.0 stylesheets](https://github.com/docbook/xslt20-stylesheets)
will allow the use modern versions of Saxon 
(and eliminate namespace-cutting messages from the XSLT 1.0 stylesheets).

Attempt to supports XSLT 2.0 stylesheets in the plugin uncovered some issues with them:
- there is no support for EPUB in the new stylesheets;
- there is no support for [PDF](https://so.nwalsh.com/2019/01/10/printingDocBook);
- HTML chunker is not `index.html`;
- HTML chunking is not deep enough;
- HTML/CSS integration doesn't work.

TODO figure out which format is well-supported (XHTML?), support it in the plugin and document.


## Past ##

Following features of the Maven Gradle plugin are not supported:
- non-customized XSL
- resolve XSL files based on the type of processing 
- expressions in <?eval?>
- access to the project and its properties in <?eval?>
- multiple executions with different documents and parameters


## Future ##

Following enhancements are being considered:
- support reuse of XSL customizations, parameters, images and CSS;
- make XSL stylesheet version configurable *or*
- do not unpack XSLT stylesheets - use them directly from the JARs that the plugin itself depends on;
- make sure EPUB2 output contains `mimetype` file;
- codify EPUB customization along the lines of [ChunkingCustomization](http://www.sagehill.net/docbookxsl/ChunkingCustomization.html) 
and epub3/README from the XSLT stlesheets distribution;
- look into running MatJax stylesheets in the plugin (without the browser) to
  convert MathML for PDF (that way, LaTeX will also be supported);
- look into XSLT solutions for MatML/LaTeX -> SVG conversion
  ([pmml2svg](http://pmml2svg.sourceforge.net/doc/user-xhtml-svg/index.xhtml)
   doesn't seem to be maintained).
