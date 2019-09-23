<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:cc="http://crossword.info/xml/crossword-compiler"
		xmlns:cca="http://crossword.info/xml/crossword-compiler-applet"
		xmlns:rp="http://crossword.info/xml/rectangular-puzzle">

	<xsl:output method="xml" indent="yes"/>
	<xsl:param name="puzzleName" />
	<xsl:param name="puzzleIdentifier" />
	<xsl:param name="puzzleNumber" />

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 - 
 - This file generates the crossword, the clues, and the answers.  The page
 - looks like this (landscape mode)
 - +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
 - | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  Across              Down             |
 - | | | | | | | | | | | | | | | | | |  1. blah, blah       1. blah, blah    |
 - | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  1. blah, blah       1. blah, blah    |
 - | | | | | | | | | | | | | | | | | |  1. blah, blah       1. blah, blah    |
 - | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  1. blah, blah       1. blah, blah    |
 - | | | | | | | | | | | | | | | | | |  1. blah, blah       1. blah, blah    |
 - | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  1. blah, blah       1. blah, blah    |
 - | | | | | | | | | | | | | | | | | |  1. blah, blah       1. blah, blah    |
 - | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  1. blah, blah       1. blah, blah    |
 - | | | | | | | | | | | | | | | | | |  1. blah, blah       1. blah, blah    |
 - | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  1. blah, blah       1. blah, blah    |
 - | | | | | | | | | | | | | | | | | |  1. blah, blah       1. blah, blah    |
 - | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  1. blah, blah       1. blah, blah    |
 - | | | | | | | | | | | | | | | | | |  1. blah, blah       1. blah, blah    |
 - | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+  1. blah, blah       1. blah, blah    |
 - |                                                                         |
 - |                                                                         |
 - |                                                        ++++++++++++++++ |
 - |                                                        ++++++++++++++++ |
 - |                                                        ++++++++++++++++ |
 - |                                                        ++++++++++++++++ |
 - |                                                        ++++++++++++++++ |
 - |                                                        ++++++++++++++++ |
 - |                                                        ++++++++++++++++ |
 - |                                                        ++++++++++++++++ |
 - |                                                        ++++++++++++++++ |
 - +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
 - 
 - XSLT Templates in order in the file:
 - 
 - name: setup-page-sizings
 -         Set up the page sizes for both landscape and portrait mode (A4)
 - name: crossword-title
 -         Generate the crossword title for the page
 - match: /cca:crossword-compiler-applet
 -         Match the XML of the crossword compiler applet namespace
 - match: /cc:crossword-compiler
 -         Match the XML of the crossword compiler namespace
 - name: crossword
 -         
 - 
 - 
 - 
v - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- 
  Set up the page sizing
  
  This is both the main part of the page, and the footer area
  
    The main part (region body) has the blank crossword, and the questions
    The footer (region-after) has the solution
  
  As the footer is static content, it must be done first.
  -->
<xsl:template name="setup-page-sizings">
	<fo:layout-master-set>
		<fo:simple-page-master 
				master-name="A4-landscape" 
				page-height="21.0cm" 
				page-width="29.7cm" 
				margin-top="1.0cm" 
				margin-left="1.0cm" 
				margin-right="0.5cm" 
				margin-bottom="0.5cm">
			<fo:region-body/>

			<!-- This is for the crossword answers -->
			<fo:region-after extent="7.0cm" reference-orientation="180"/>

		</fo:simple-page-master>
	</fo:layout-master-set>
</xsl:template>

<xsl:template name="crossword-title">
	<fo:block margin-bottom="1mm" border-bottom="solid">
		<fo:inline font-size="18pt" font-weight="bold" font-family="serif">
			<xsl:value-of select="$puzzleName" /> 
			<xsl:if test="$puzzleNumber != -1">
				(#<xsl:value-of select="$puzzleNumber" />)
			</xsl:if> - 
		</fo:inline>
		<fo:inline font-size="10pt" font-family="serif"><xsl:value-of select="$puzzleIdentifier" /></fo:inline>
	</fo:block>
</xsl:template>

<!--
  For those xml crosswords which are applet namespaced - this is the solution
  -->
<xsl:template match="/cca:crossword-compiler-applet">
	<xsl:call-template name="crossword">
		<xsl:with-param name="crossword-data" select="/cca:crossword-compiler-applet" />
	</xsl:call-template>
</xsl:template>

<!--
  For those xml crosswords which are normally namespaced - this is the solution
  -->
<xsl:template match="/cc:crossword-compiler">
	<xsl:call-template name="crossword">
		<xsl:with-param name="crossword-data" select="/cc:crossword-compiler" />
	</xsl:call-template>
</xsl:template>

<xsl:template name="crossword">
	<xsl:param name="crossword-data" />

	<fo:root>
		<xsl:call-template name="setup-page-sizings" />

		<fo:page-sequence master-reference="A4-landscape">

<fo:static-content flow-name="xsl-region-after">
	<xsl:for-each select="$crossword-data/rp:rectangular-puzzle/rp:crossword/rp:grid/rp:cell">

		<xsl:if test="@x = 1">
			<fo:block font-size="4pt">
				<fo:table border-collapse="collapse" border-color="#444444">

					<fo:table-body>
						<fo:table-row>

							<xsl:call-template name="table-row-solution">
								<xsl:with-param name="nodes" select="$crossword-data" />
								<xsl:with-param name="position" select="position()" />
							</xsl:call-template>

							<fo:table-cell><!-- EMPTY - TO FORCE RIGHT ALIGN --><fo:block /></fo:table-cell>

						</fo:table-row>
					</fo:table-body>
				</fo:table>
			</fo:block>

		</xsl:if>
	</xsl:for-each>
</fo:static-content>

			<!-- 
			  Start of the blank crossword, and the questions
			   -->
			<fo:flow flow-name="xsl-region-body">

				<xsl:call-template name="crossword-title" />

				<xsl:call-template name="table-empty-crossword">
					<xsl:with-param name="nodes" select="$crossword-data" />
				</xsl:call-template>

			</fo:flow>

		</fo:page-sequence>

	</fo:root>
</xsl:template>
<!-- 
  This is the Crossword grid
  -->
<xsl:template name="table-row">
	<xsl:param name = "position" />
	<xsl:param name = "nodes" />

	<xsl:variable name="width" select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/@width" />
	<xsl:variable name="height" select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/@height" />

	<xsl:for-each select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/rp:cell[@y = ((($position -1) mod $height) + 1)]">
		<xsl:choose>
			<xsl:when test="$width > 17">
			<xsl:variable name="boxWidth"><xsl:value-of select="8 - ($width - 17) + 1" />mm</xsl:variable>

				<xsl:choose>
					<xsl:when test="not(count(./@type) = 0)">
						<fo:table-cell border="solid" border-collapse="collapse" width="7mm" height="7mm" background-color="black">
							<fo:block />
						</fo:table-cell>
					</xsl:when>
					<xsl:otherwise>
						<fo:table-cell border="solid" border-collapse="collapse" width="7mm" height="7mm" padding-left="0.6mm"  font-size="8pt">
							<fo:block ><xsl:value-of select="./@number" /></fo:block>
						</fo:table-cell>
					</xsl:otherwise>
				</xsl:choose>

			</xsl:when>
			<xsl:otherwise>

				<xsl:choose>
					<xsl:when test="not(count(./@type) = 0)">
						<fo:table-cell border="solid" border-collapse="collapse" width="8mm" height="8mm" background-color="black">
							<fo:block />
						</fo:table-cell>
					</xsl:when>
					<xsl:otherwise>
						<fo:table-cell border="solid" border-collapse="collapse" width="8mm" height="8mm" padding-left="0.6mm"  font-size="8pt">
							<fo:block ><xsl:value-of select="./@number" /></fo:block>
						</fo:table-cell>
					</xsl:otherwise>
				</xsl:choose>

			</xsl:otherwise>


		</xsl:choose>

	</xsl:for-each>

</xsl:template>

<!-- 
  Now for the empty table
  -->
<xsl:template name="table-empty-crossword">
	<xsl:param name = "nodes" />

	<xsl:variable name="width" select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/@width" />
	<xsl:variable name="height" select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/@height" />

	<fo:table border-collapse="collapse">

		<fo:table-body>
			<fo:table-row>
				<fo:table-cell>

					<xsl:for-each select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/rp:cell">

						<xsl:if test="@x = 1">

							<fo:block font-size="10pt">
								<fo:table border-collapse="collapse">

									<fo:table-body>
										<fo:table-row>

											<xsl:call-template name="table-row">
												<xsl:with-param name="position" select="position()" />
												<xsl:with-param name="nodes" select="$nodes" />
											</xsl:call-template>

										</fo:table-row>
									</fo:table-body>
								</fo:table>
							</fo:block>

						</xsl:if>
					</xsl:for-each>
				</fo:table-cell>

				<fo:table-cell font-size="8pt">
					<fo:block>
					<fo:table border-collapse="collapse">
						<fo:table-body>
							<fo:table-row>
								<xsl:for-each select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:clues">
									<fo:table-cell border-collapse="collapse" margin-right="2mm">
										<fo:block font-weight="bold">
											<xsl:value-of select="./rp:title" />
										</fo:block>

										<fo:table border-collapse="collapse">
											<fo:table-body>
												<xsl:for-each select="./rp:clue">
													<fo:table-row>
														<fo:table-cell>
															<xsl:choose>
																<xsl:when test="@format = ''">
																	<fo:block><xsl:value-of select="@number" />. <xsl:value-of select="." /></fo:block>
																</xsl:when>
																<xsl:otherwise>
																	<fo:block><xsl:value-of select="@number" />. <xsl:value-of select="." /> (<xsl:value-of select="@format" />)</fo:block>
																</xsl:otherwise>
															</xsl:choose>
														</fo:table-cell>
													</fo:table-row>
												</xsl:for-each>
											</fo:table-body>
										</fo:table>
									</fo:table-cell>

								</xsl:for-each>
							</fo:table-row>

						</fo:table-body>
					</fo:table>
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</fo:table-body>
	</fo:table>
</xsl:template>

<!--
	Now for the solution grid
	-->
<xsl:template name="table-row-solution">
	<xsl:param name = "nodes" />
	<xsl:param name = "position" />

	<xsl:variable name="width" select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/@width" />
	<xsl:variable name="height" select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/@height" />

	<xsl:for-each select="$nodes/rp:rectangular-puzzle/rp:crossword/rp:grid/rp:cell[@y = ((($position -1) mod $height) + 1)]">

		<xsl:choose>
			<xsl:when test="not(count(./@type) = 0)">
				<fo:table-cell border="solid" border-collapse="collapse" width="4mm" height="4mm" background-color="#999999" border-color="#999999">
					<fo:block />
				</fo:table-cell>
			</xsl:when>
			<xsl:otherwise>
				<fo:table-cell border="solid" border-collapse="collapse" width="4mm" height="4mm" padding-left="0.6mm" border-color="#999999">

					<xsl:choose>
						<xsl:when test="count(./@number) = 0">
							<fo:block color="white">1</fo:block>
						</xsl:when>
						<xsl:otherwise>
							<fo:block color="#999999">
								<xsl:value-of select="./@number" />
							</fo:block>
						</xsl:otherwise>
					</xsl:choose>

					<fo:block
							font-size="8pt" 
							font-weight="bold" 
							text-align="center" 
							margin-top="-1mm" 
							margin-bottom="0" 
							margin-left="0mm" 
							padding="0"
							color="#999999"
							font-family="monospace">
						<xsl:value-of select="./@solution" />
					</fo:block>

				</fo:table-cell>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:for-each>

</xsl:template>
</xsl:stylesheet>