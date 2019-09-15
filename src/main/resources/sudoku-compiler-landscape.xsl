<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:output method="xml" indent="yes"/>
	<xsl:param name="puzzleName" />
	<xsl:param name="puzzleIdentifier" />
	<xsl:param name="puzzleNumber" />

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

		</fo:simple-page-master>
	</fo:layout-master-set>
</xsl:template>

<xsl:template name="puzzle-title">
	<fo:block margin-bottom="8mm" border-bottom="solid">
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
  xml sudoku format
  -->
<xsl:template match="/sudoku">
	<fo:root>
		<xsl:call-template name="setup-page-sizings" />

		<fo:page-sequence master-reference="A4-landscape">
			<fo:flow flow-name="xsl-region-body">
			<xsl:call-template name="puzzle-title" />

			<fo:table border-collapse="collapse">
		
				<fo:table-body>

					<xsl:for-each select="row">
						<fo:table-row>

							<xsl:for-each select="cell">
								<xsl:choose>
									<xsl:when test="((@y=4 or @y=5 or @y=6) and (@x=4 or @x=5 or @x=6)) or ((@x=1 or @x=2 or @x=3 or @x=7 or @x=8 or @x=9) and (@y = 1 or @y = 2 or @y=3 or @y=7 or @y=8 or @y=9))">

										<fo:table-cell border="solid" border-width="1.6px" border-collapse="collapse" width="7mm" height="7mm" padding-left="0.6mm" 
												text-align="center" font-size="14pt" display-align="after" background-color="#dddddd">
											<fo:block >
												<xsl:if test="@show = 'True'">
													<xsl:value-of select="@value" />
												</xsl:if>
											</fo:block>
										</fo:table-cell>

									</xsl:when>
									<xsl:otherwise>

										<fo:table-cell border="solid" border-collapse="collapse" width="7mm" height="7mm" padding-left="0.6mm" text-align="center" font-size="14pt" display-align="after">
											<fo:block >
												<xsl:if test="@show = 'True'">
													<xsl:value-of select="@value" />
												</xsl:if>
											</fo:block>
										</fo:table-cell>

									</xsl:otherwise>
								</xsl:choose>

							</xsl:for-each>

						</fo:table-row>
					</xsl:for-each>

				</fo:table-body>
			</fo:table>
			</fo:flow>
		</fo:page-sequence>
	</fo:root>
</xsl:template>

</xsl:stylesheet>