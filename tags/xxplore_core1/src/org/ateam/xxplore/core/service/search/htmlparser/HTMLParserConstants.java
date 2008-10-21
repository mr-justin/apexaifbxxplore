/* Generated By:JavaCC: Do not edit this line. HTMLParserConstants.java */
package org.ateam.xxplore.core.service.search.htmlparser;

public interface HTMLParserConstants {

  int EOF = 0;
  int ScriptStart = 1;
  int TagName = 2;
  int DeclName = 3;
  int Comment1 = 4;
  int Comment2 = 5;
  int Word = 6;
  int LET = 7;
  int NUM = 8;
  int HEX = 9;
  int Entity = 10;
  int Space = 11;
  int SP = 12;
  int Punct = 13;
  int ScriptText = 14;
  int ScriptEnd = 15;
  int ArgName = 16;
  int ArgEquals = 17;
  int TagEnd = 18;
  int ArgValue = 19;
  int ArgQuote1 = 20;
  int ArgQuote2 = 21;
  int Quote1Text = 23;
  int CloseQuote1 = 24;
  int Quote2Text = 25;
  int CloseQuote2 = 26;
  int CommentText1 = 27;
  int CommentEnd1 = 28;
  int CommentText2 = 29;
  int CommentEnd2 = 30;

  int DEFAULT = 0;
  int WithinScript = 1;
  int WithinTag = 2;
  int AfterEquals = 3;
  int WithinQuote1 = 4;
  int WithinQuote2 = 5;
  int WithinComment1 = 6;
  int WithinComment2 = 7;

  String[] tokenImage = {
    "<EOF>",
    "\"<script\"",
    "<TagName>",
    "<DeclName>",
    "\"<!--\"",
    "\"<!\"",
    "<Word>",
    "<LET>",
    "<NUM>",
    "<HEX>",
    "<Entity>",
    "<Space>",
    "<SP>",
    "<Punct>",
    "<ScriptText>",
    "<ScriptEnd>",
    "<ArgName>",
    "\"=\"",
    "<TagEnd>",
    "<ArgValue>",
    "\"\\\'\"",
    "\"\\\"\"",
    "<token of kind 22>",
    "<Quote1Text>",
    "<CloseQuote1>",
    "<Quote2Text>",
    "<CloseQuote2>",
    "<CommentText1>",
    "\"-->\"",
    "<CommentText2>",
    "\">\"",
  };

}
