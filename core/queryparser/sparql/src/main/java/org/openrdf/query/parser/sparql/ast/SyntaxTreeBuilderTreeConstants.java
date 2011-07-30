/* Generated By:JavaCC: Do not edit this line. SyntaxTreeBuilderTreeConstants.java Version 5.0 */
package org.openrdf.query.parser.sparql.ast;

public interface SyntaxTreeBuilderTreeConstants
{
  public int JJTQUERYCONTAINER = 0;
  public int JJTVOID = 1;
  public int JJTBASEDECL = 2;
  public int JJTPREFIXDECL = 3;
  public int JJTSELECTQUERY = 4;
  public int JJTSELECT = 5;
  public int JJTPROJECTIONELEM = 6;
  public int JJTCONSTRUCTQUERY = 7;
  public int JJTCONSTRUCT = 8;
  public int JJTDESCRIBEQUERY = 9;
  public int JJTDESCRIBE = 10;
  public int JJTASKQUERY = 11;
  public int JJTDATASETCLAUSE = 12;
  public int JJTWHERECLAUSE = 13;
  public int JJTGROUPCLAUSE = 14;
  public int JJTORDERCLAUSE = 15;
  public int JJTGROUPCONDITION = 16;
  public int JJTHAVINGCLAUSE = 17;
  public int JJTORDERCONDITION = 18;
  public int JJTLIMIT = 19;
  public int JJTOFFSET = 20;
  public int JJTGRAPHPATTERNGROUP = 21;
  public int JJTBASICGRAPHPATTERN = 22;
  public int JJTOPTIONALGRAPHPATTERN = 23;
  public int JJTGRAPHGRAPHPATTERN = 24;
  public int JJTUNIONGRAPHPATTERN = 25;
  public int JJTMINUSGRAPHPATTERN = 26;
  public int JJTCONSTRAINT = 27;
  public int JJTFUNCTIONCALL = 28;
  public int JJTTRIPLESSAMESUBJECT = 29;
  public int JJTPROPERTYLIST = 30;
  public int JJTOBJECTLIST = 31;
  public int JJTTRIPLESSAMESUBJECTPATH = 32;
  public int JJTPROPERTYLISTPATH = 33;
  public int JJTPATHALTERNATIVE = 34;
  public int JJTPATHSEQUENCE = 35;
  public int JJTPATHELT = 36;
  public int JJTIRI = 37;
  public int JJTPATHONEINPROPERTYSET = 38;
  public int JJTPATHMOD = 39;
  public int JJTBLANKNODEPROPERTYLIST = 40;
  public int JJTCOLLECTION = 41;
  public int JJTVAR = 42;
  public int JJTOR = 43;
  public int JJTAND = 44;
  public int JJTCOMPARE = 45;
  public int JJTMATH = 46;
  public int JJTNOT = 47;
  public int JJTNUMERICLITERAL = 48;
  public int JJTCOUNT = 49;
  public int JJTSUM = 50;
  public int JJTMIN = 51;
  public int JJTMAX = 52;
  public int JJTAVG = 53;
  public int JJTSAMPLE = 54;
  public int JJTGROUPCONCAT = 55;
  public int JJTNOW = 56;
  public int JJTYEAR = 57;
  public int JJTMONTH = 58;
  public int JJTDAY = 59;
  public int JJTHOURS = 60;
  public int JJTMINUTES = 61;
  public int JJTSECONDS = 62;
  public int JJTTIMEZONE = 63;
  public int JJTTZ = 64;
  public int JJTRAND = 65;
  public int JJTABS = 66;
  public int JJTCEIL = 67;
  public int JJTFLOOR = 68;
  public int JJTROUND = 69;
  public int JJTSUBSTR = 70;
  public int JJTSTRLEN = 71;
  public int JJTUPPERCASE = 72;
  public int JJTLOWERCASE = 73;
  public int JJTSTRSTARTS = 74;
  public int JJTSTRENDS = 75;
  public int JJTCONCAT = 76;
  public int JJTCONTAINS = 77;
  public int JJTENCODEFORURI = 78;
  public int JJTIF = 79;
  public int JJTIN = 80;
  public int JJTNOTIN = 81;
  public int JJTCOALESCE = 82;
  public int JJTSTR = 83;
  public int JJTLANG = 84;
  public int JJTLANGMATCHES = 85;
  public int JJTDATATYPE = 86;
  public int JJTBOUND = 87;
  public int JJTSAMETERM = 88;
  public int JJTISIRI = 89;
  public int JJTISBLANK = 90;
  public int JJTISLITERAL = 91;
  public int JJTISNUMERIC = 92;
  public int JJTBNODEFUNC = 93;
  public int JJTIRIFUNC = 94;
  public int JJTSTRDT = 95;
  public int JJTSTRLANG = 96;
  public int JJTBIND = 97;
  public int JJTREGEXEXPRESSION = 98;
  public int JJTEXISTSFUNC = 99;
  public int JJTNOTEXISTSFUNC = 100;
  public int JJTRDFLITERAL = 101;
  public int JJTTRUE = 102;
  public int JJTFALSE = 103;
  public int JJTSTRING = 104;
  public int JJTQNAME = 105;
  public int JJTBLANKNODE = 106;


  public String[] jjtNodeName = {
    "QueryContainer",
    "void",
    "BaseDecl",
    "PrefixDecl",
    "SelectQuery",
    "Select",
    "ProjectionElem",
    "ConstructQuery",
    "Construct",
    "DescribeQuery",
    "Describe",
    "AskQuery",
    "DatasetClause",
    "WhereClause",
    "GroupClause",
    "OrderClause",
    "GroupCondition",
    "HavingClause",
    "OrderCondition",
    "Limit",
    "Offset",
    "GraphPatternGroup",
    "BasicGraphPattern",
    "OptionalGraphPattern",
    "GraphGraphPattern",
    "UnionGraphPattern",
    "MinusGraphPattern",
    "Constraint",
    "FunctionCall",
    "TriplesSameSubject",
    "PropertyList",
    "ObjectList",
    "TriplesSameSubjectPath",
    "PropertyListPath",
    "PathAlternative",
    "PathSequence",
    "PathElt",
    "IRI",
    "PathOneInPropertySet",
    "PathMod",
    "BlankNodePropertyList",
    "Collection",
    "Var",
    "Or",
    "And",
    "Compare",
    "Math",
    "Not",
    "NumericLiteral",
    "Count",
    "Sum",
    "Min",
    "Max",
    "Avg",
    "Sample",
    "GroupConcat",
    "Now",
    "Year",
    "Month",
    "Day",
    "Hours",
    "Minutes",
    "Seconds",
    "Timezone",
    "Tz",
    "Rand",
    "Abs",
    "Ceil",
    "Floor",
    "Round",
    "Substr",
    "StrLen",
    "UpperCase",
    "LowerCase",
    "StrStarts",
    "StrEnds",
    "Concat",
    "Contains",
    "EncodeForURI",
    "If",
    "In",
    "NotIn",
    "Coalesce",
    "Str",
    "Lang",
    "LangMatches",
    "Datatype",
    "Bound",
    "SameTerm",
    "IsIRI",
    "IsBlank",
    "IsLiteral",
    "IsNumeric",
    "BNodeFunc",
    "IRIFunc",
    "StrDt",
    "StrLang",
    "Bind",
    "RegexExpression",
    "ExistsFunc",
    "NotExistsFunc",
    "RDFLiteral",
    "True",
    "False",
    "String",
    "QName",
    "BlankNode",
  };
}
/* JavaCC - OriginalChecksum=8c07420117f33ff50d15d643361c0eeb (do not edit this line) */
