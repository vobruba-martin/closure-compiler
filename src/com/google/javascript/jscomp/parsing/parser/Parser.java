/*
 * Copyright 2011 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp.parsing.parser;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.google.javascript.jscomp.parsing.parser.FeatureSet.Feature;
import com.google.javascript.jscomp.parsing.parser.trees.ArgumentListTree;
import com.google.javascript.jscomp.parsing.parser.trees.ArrayLiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ArrayPatternTree;
import com.google.javascript.jscomp.parsing.parser.trees.AwaitExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.BinaryOperatorTree;
import com.google.javascript.jscomp.parsing.parser.trees.BlockTree;
import com.google.javascript.jscomp.parsing.parser.trees.BreakStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.CallExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.CaseClauseTree;
import com.google.javascript.jscomp.parsing.parser.trees.CatchTree;
import com.google.javascript.jscomp.parsing.parser.trees.ClassDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.CommaExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.Comment;
import com.google.javascript.jscomp.parsing.parser.trees.ComprehensionForTree;
import com.google.javascript.jscomp.parsing.parser.trees.ComprehensionIfTree;
import com.google.javascript.jscomp.parsing.parser.trees.ComprehensionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ComputedPropertyDefinitionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ComputedPropertyFieldTree;
import com.google.javascript.jscomp.parsing.parser.trees.ComputedPropertyGetterTree;
import com.google.javascript.jscomp.parsing.parser.trees.ComputedPropertyMethodTree;
import com.google.javascript.jscomp.parsing.parser.trees.ComputedPropertySetterTree;
import com.google.javascript.jscomp.parsing.parser.trees.ConditionalExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ContinueStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.DebuggerStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.DefaultClauseTree;
import com.google.javascript.jscomp.parsing.parser.trees.DefaultParameterTree;
import com.google.javascript.jscomp.parsing.parser.trees.DoWhileStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.DynamicImportTree;
import com.google.javascript.jscomp.parsing.parser.trees.EmptyStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.ExportDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.ExportSpecifierTree;
import com.google.javascript.jscomp.parsing.parser.trees.ExpressionStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.FieldDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.FinallyTree;
import com.google.javascript.jscomp.parsing.parser.trees.ForAwaitOfStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.ForInStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.ForOfStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.ForStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.FormalParameterListTree;
import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.GetAccessorTree;
import com.google.javascript.jscomp.parsing.parser.trees.IdentifierExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.IfStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.ImportDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.ImportMetaExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ImportSpecifierTree;
import com.google.javascript.jscomp.parsing.parser.trees.IterRestTree;
import com.google.javascript.jscomp.parsing.parser.trees.IterSpreadTree;
import com.google.javascript.jscomp.parsing.parser.trees.LabelledStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.LiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.MemberExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.MemberLookupExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.MissingPrimaryExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.NewExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.NewTargetExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.NullTree;
import com.google.javascript.jscomp.parsing.parser.trees.ObjectLiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ObjectPatternTree;
import com.google.javascript.jscomp.parsing.parser.trees.ObjectRestTree;
import com.google.javascript.jscomp.parsing.parser.trees.ObjectSpreadTree;
import com.google.javascript.jscomp.parsing.parser.trees.OptChainCallExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.OptionalMemberExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.OptionalMemberLookupExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParenExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTreeType;
import com.google.javascript.jscomp.parsing.parser.trees.ProgramTree;
import com.google.javascript.jscomp.parsing.parser.trees.PropertyNameAssignmentTree;
import com.google.javascript.jscomp.parsing.parser.trees.ReturnStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.SetAccessorTree;
import com.google.javascript.jscomp.parsing.parser.trees.SuperExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.SwitchStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.TemplateLiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.TemplateLiteralPortionTree;
import com.google.javascript.jscomp.parsing.parser.trees.TemplateSubstitutionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ThisExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.ThrowStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.TryStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.UnaryExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.UpdateExpressionTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableDeclarationListTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.VariableStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.WhileStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.WithStatementTree;
import com.google.javascript.jscomp.parsing.parser.trees.YieldExpressionTree;
import com.google.javascript.jscomp.parsing.parser.util.ErrorReporter;
import com.google.javascript.jscomp.parsing.parser.util.SourcePosition;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import java.util.ArrayDeque;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Parses a javascript file.
 *
 * <p>The various parseX() methods never return null - even when parse errors are encountered.
 * Typically parseX() will return a XTree ParseTree. Each ParseTree that is created includes its
 * source location. The typical pattern for a parseX() method is:
 *
 * <pre>
 * XTree parseX() {
 *   SourcePosition start = getTreeStartLocation();
 *   parse X grammar element and its children
 *   return new XTree(getTreeLocation(start), children);
 * }
 * </pre>
 *
 * <p>parseX() methods must consume at least 1 token - even in error cases. This prevents infinite
 * loops in the parser.
 *
 * <p>Many parseX() methods are matched by a 'boolean peekX()' method which will return true if the
 * beginning of an X appears at the current location. There are also peek() methods which examine
 * the next token. peek() methods must not consume any tokens.
 *
 * <p>The eat() method consumes a token and reports an error if the consumed token is not of the
 * expected type. The eatOpt() methods consume the next token iff the next token is of the expected
 * type and return the consumed token or null if no token was consumed.
 *
 * <p>When parse errors are encountered, an error should be reported and the parse should return a
 * best guess at the current parse tree.
 *
 * <p>When parsing lists, the preferred pattern is:
 *
 * <pre>
 *   eat(LIST_START);
 *   ImmutableList.Builder&lt;ParseTree&gt; elements = ImmutableList.builder();
 *   while (peekListElement()) {
 *     elements.add(parseListElement());
 *   }
 *   eat(LIST_END);
 * </pre>
 */
public class Parser {
  /** Indicates the type of function currently being parsed. */
  private enum FunctionFlavor {
    NORMAL(false, false),
    GENERATOR(true, false),
    ASYNCHRONOUS(false, true),
    ASYNCHRONOUS_GENERATOR(true, true);

    final boolean isGenerator;
    final boolean isAsynchronous;

    FunctionFlavor(boolean isGenerator, boolean isAsynchronous) {
      this.isGenerator = isGenerator;
      this.isAsynchronous = isAsynchronous;
    }
  }

  private final Scanner scanner;
  private final ErrorReporter errorReporter;
  private final Config config;
  private final CommentRecorder commentRecorder = new CommentRecorder();
  private final ArrayDeque<FunctionFlavor> functionContextStack = new ArrayDeque<>();
  private FeatureSet features = FeatureSet.BARE_MINIMUM;
  private SourcePosition lastSourcePosition;
  private @Nullable String sourceMapURL;

  public Parser(Config config, ErrorReporter errorReporter, SourceFile source) {
    this.config = config;
    this.errorReporter = errorReporter;
    this.scanner = new Scanner(errorReporter, commentRecorder, source, 0);
    this.functionContextStack.addLast(FunctionFlavor.NORMAL);
    lastSourcePosition = scanner.getPosition();
  }

  public static class Config {
    public static enum Mode {
      ES3,
      ES5,
      ES6_OR_ES7,
      ES8_OR_GREATER,
    }

    private final boolean atLeast6;
    private final boolean atLeast8;
    private final boolean isStrictMode;
    private final boolean warnTrailingCommas;

    public Config() {
      this(Mode.ES8_OR_GREATER, /* isStrictMode= */ false);
    }

    public Config(Mode mode, boolean isStrictMode) {
      atLeast6 = !(mode == Mode.ES3 || mode == Mode.ES5);
      atLeast8 = mode == Mode.ES8_OR_GREATER;
      this.isStrictMode = isStrictMode;

      // Generally, we allow everything that is valid in any mode
      // we only warn about things that are not represented in the AST.
      this.warnTrailingCommas = mode == Mode.ES3;
    }
  }

  private static final String SOURCE_MAPPING_URL_PREFIX = "//# sourceMappingURL=";

  private class CommentRecorder implements Scanner.CommentRecorder {
    private final ImmutableList.Builder<Comment> comments = ImmutableList.builder();
    private SourcePosition lastCommentEndPosition;

    @Override
    public void recordComment(Comment.Type type, SourceRange range, String value) {
      // If we rewind the token stream, the scanner might pass comments that we've already seen.
      // Only record comments past the furthest comment end position we've seen.
      // NB: this assumes the CommentRecorder is used for at most one source file.
      if (lastCommentEndPosition == null || range.end.offset > this.lastCommentEndPosition.offset) {
        value = value.trim();
        if (value.startsWith(SOURCE_MAPPING_URL_PREFIX)) {
          sourceMapURL = value.substring(SOURCE_MAPPING_URL_PREFIX.length());
        }
        comments.add(new Comment(value, range, type));
        this.lastCommentEndPosition = range.end;
      }
    }

    private ImmutableList<Comment> getComments() {
      return comments.build();
    }
  }

  public List<Comment> getComments() {
    return commentRecorder.getComments();
  }

  public FeatureSet getFeatures() {
    return features;
  }

  /** Returns the url provided by the sourceMappingURL if any was found. */
  public @Nullable String getSourceMapURL() {
    return sourceMapURL;
  }

  // 14 Program
  public @Nullable ProgramTree parseProgram() {
    try {
      // Set the start location at the beginning of the file rather than the beginning of the first
      // token.  This ensures that it accounts for leading comments.
      SourcePosition start = lastSourcePosition;
      ImmutableList<ParseTree> sourceElements = parseGlobalSourceElements();
      eat(TokenType.END_OF_FILE);
      return new ProgramTree(getTreeLocation(start), sourceElements, commentRecorder.getComments());
    } catch (Error e) {
      // We are checking the error message instead of catching StackOverflowError since
      // StackOverflowError is not emulated on the Web.
      if (e.toString().contains("java.lang.StackOverflowError")) {
        reportError("Too deep recursion while parsing");
        return null;
      }
      throw e;
    }
  }

  private ImmutableList<ParseTree> parseGlobalSourceElements() {
    ImmutableList.Builder<ParseTree> result = ImmutableList.builder();

    while (!peek(TokenType.END_OF_FILE)) {
      result.add(parseScriptElement());
    }

    return result.build();
  }

  // ImportDeclaration
  // ExportDeclaration
  // SourceElement
  private ParseTree parseScriptElement() {
    if (peekImportDeclaration()) {
      return parseImportDeclaration();
    }

    if (peekExportDeclaration()) {
      return parseExportDeclaration();
    }

    return parseSourceElement();
  }

  private boolean peekImportDeclaration() {
    return peek(TokenType.IMPORT)
        && (peekIdOrKeyword(1)
            || peek(1, TokenType.STRING)
            || peek(1, TokenType.OPEN_CURLY)
            || peek(1, TokenType.STAR));
  }

  private ParseTree parseImportDeclaration() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.IMPORT);

    // import ModuleSpecifier ;
    if (peek(TokenType.STRING)) {
      LiteralToken moduleSpecifier = eat(TokenType.STRING).asLiteral();
      eatPossiblyImplicitSemiColon();

      return new ImportDeclarationTree(getTreeLocation(start), null, null, null, moduleSpecifier);
    }

    // import ImportedDefaultBinding from ModuleSpecifier
    // import NameSpaceImport from ModuleSpecifier
    // import NamedImports from ModuleSpecifier ;
    // import ImportedDefaultBinding , NameSpaceImport from ModuleSpecifier ;
    // import ImportedDefaultBinding , NamedImports from ModuleSpecifier ;
    IdentifierToken defaultBindingIdentifier = null;
    IdentifierToken nameSpaceImportIdentifier = null;
    ImmutableList<ParseTree> identifierSet = null;

    boolean parseExplicitNames = true;
    if (peekId()) {
      defaultBindingIdentifier = eatId();
      if (peek(TokenType.COMMA)) {
        eat(TokenType.COMMA);
      } else {
        parseExplicitNames = false;
      }
    } else if (Keywords.isKeyword(peekType())) {
      Token keyword = nextToken();
      reportError(keyword, "cannot use keyword '%s' here.", keyword);
    }

    if (parseExplicitNames) {
      if (peek(TokenType.STAR)) {
        eat(TokenType.STAR);
        eatPredefinedString(PredefinedName.AS);
        nameSpaceImportIdentifier = eatId();
      } else {
        identifierSet = parseImportSpecifierSet();
      }
    }

    eatPredefinedString(PredefinedName.FROM);
    Token moduleStr = eat(TokenType.STRING);
    LiteralToken moduleSpecifier = (moduleStr == null) ? null : moduleStr.asLiteral();
    eatPossiblyImplicitSemiColon();

    return new ImportDeclarationTree(
        getTreeLocation(start),
        defaultBindingIdentifier,
        identifierSet,
        nameSpaceImportIdentifier,
        moduleSpecifier);
  }

  //  ImportSpecifierSet ::= '{' (ImportSpecifier (',' ImportSpecifier)* (,)? )?  '}'
  private ImmutableList<ParseTree> parseImportSpecifierSet() {
    ImmutableList.Builder<ParseTree> elements = ImmutableList.builder();
    eat(TokenType.OPEN_CURLY);
    while (peekIdOrKeyword()) {
      elements.add(parseImportSpecifier());
      if (!peek(TokenType.CLOSE_CURLY)) {
        eat(TokenType.COMMA);
      }
    }
    eat(TokenType.CLOSE_CURLY);
    return elements.build();
  }

  //  ImportSpecifier ::= Identifier ('as' Identifier)?
  private ParseTree parseImportSpecifier() {
    SourcePosition start = getTreeStartLocation();
    IdentifierToken importedName = eatIdOrKeywordAsId();
    IdentifierToken destinationName = null;
    if (peekPredefinedString(PredefinedName.AS)) {
      eatPredefinedString(PredefinedName.AS);
      destinationName = eatId();
    } else if (importedName.isKeyword()) {
      reportExpectedError(null, PredefinedName.AS);
    }
    return new ImportSpecifierTree(getTreeLocation(start), importedName, destinationName);
  }

  // export  VariableStatement
  // export  FunctionDeclaration
  // export  ConstStatement
  // export  ClassDeclaration
  // export  default expression
  // etc
  private boolean peekExportDeclaration() {
    return peek(TokenType.EXPORT);
  }

  /*
  ExportDeclaration :
    export * FromClause ;
    export ExportClause [NoReference] FromClause ;
    export ExportClause ;
    export VariableStatement
    export Declaration[Default]
    export default AssignmentExpression ;
  ExportClause [NoReference] :
    { }
    { ExportsList [?NoReference] }
    { ExportsList [?NoReference] , }
  ExportsList [NoReference] :
    ExportSpecifier [?NoReference]
    ExportsList [?NoReference] , ExportSpecifier [?NoReference]
  ExportSpecifier [NoReference] :
    [~NoReference] IdentifierReference
    [~NoReference] IdentifierReference as IdentifierName
    [+NoReference] IdentifierName
    [+NoReference] IdentifierName as IdentifierName
   */
  private ParseTree parseExportDeclaration() {
    SourcePosition start = getTreeStartLocation();
    boolean isDefault = false;
    boolean isExportAll = false;
    boolean isExportSpecifier = false;
    boolean needsSemiColon = true;
    eat(TokenType.EXPORT);
    ParseTree export = null;
    ImmutableList<ParseTree> exportSpecifierList = null;
    switch (peekType()) {
      case STAR:
        isExportAll = true;
        nextToken();
        break;
      case IDENTIFIER:
        export = parseAsyncFunctionDeclaration();
        break;
      case FUNCTION:
        export = parseFunctionDeclaration();
        needsSemiColon = false;
        break;
      case CLASS:
        export = parseClassDeclaration();
        needsSemiColon = false;
        break;
      case DEFAULT:
        isDefault = true;
        nextToken();
        export = parseExpression();
        needsSemiColon = false;
        break;
      case OPEN_CURLY:
        isExportSpecifier = true;
        exportSpecifierList = parseExportSpecifierSet();
        break;
      case VAR:
      case LET:
      case CONST:
      default: // unreachable, parse as a var decl to get a parse error.
        export = parseVariableDeclarationList();
        break;
    }

    LiteralToken moduleSpecifier = null;
    if (isExportAll || (isExportSpecifier && peekPredefinedString(PredefinedName.FROM))) {
      eatPredefinedString(PredefinedName.FROM);
      moduleSpecifier = (LiteralToken) eat(TokenType.STRING);
    } else if (isExportSpecifier) {
      for (ParseTree tree : exportSpecifierList) {
        IdentifierToken importedName = tree.asExportSpecifier().importedName;
        if (importedName.isKeyword()) {
          reportError(importedName, "cannot use keyword '%s' here.", importedName);
        }
      }
    }

    if (needsSemiColon || peekImplicitSemiColon()) {
      eatPossiblyImplicitSemiColon();
    }

    return new ExportDeclarationTree(
        getTreeLocation(start),
        isDefault,
        isExportAll,
        export,
        exportSpecifierList,
        moduleSpecifier);
  }

  //  ExportSpecifierSet ::= '{' (ExportSpecifier (',' ExportSpecifier)* (,)? )?  '}'
  private ImmutableList<ParseTree> parseExportSpecifierSet() {
    ImmutableList.Builder<ParseTree> elements = ImmutableList.builder();
    eat(TokenType.OPEN_CURLY);
    while (peekIdOrKeyword()) {
      elements.add(parseExportSpecifier());
      if (!peek(TokenType.CLOSE_CURLY)) {
        eat(TokenType.COMMA);
      }
    }
    eat(TokenType.CLOSE_CURLY);
    return elements.build();
  }

  //  ExportSpecifier ::= Identifier ('as' Identifier)?
  private ParseTree parseExportSpecifier() {
    SourcePosition start = getTreeStartLocation();
    IdentifierToken importedName = eatIdOrKeywordAsId();
    IdentifierToken destinationName = null;
    if (peekPredefinedString(PredefinedName.AS)) {
      eatPredefinedString(PredefinedName.AS);
      destinationName = eatIdOrKeywordAsId();
    }
    return new ExportSpecifierTree(getTreeLocation(start), importedName, destinationName);
  }

  private boolean peekClassDeclaration() {
    return peek(TokenType.CLASS);
  }

  private ParseTree parseClassDeclaration() {
    return parseClass(/* isExpression= */ false);
  }

  private ParseTree parseClassExpression() {
    return parseClass(/* isExpression= */ true);
  }

  private ParseTree parseClass(boolean isExpression) {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.CLASS);
    IdentifierToken name = null;
    if (!isExpression || peekId()) {
      name = eatId();
    }

    ParseTree superClass = null;
    if (peek(TokenType.EXTENDS)) {
      eat(TokenType.EXTENDS);
      superClass = parseLeftHandSideExpression();
    }

    eat(TokenType.OPEN_CURLY);
    ImmutableList<ParseTree> elements = parseClassElements();
    eat(TokenType.CLOSE_CURLY);
    return new ClassDeclarationTree(getTreeLocation(start), name, superClass, elements);
  }

  private ImmutableList<ParseTree> parseClassElements() {
    ImmutableList.Builder<ParseTree> result = ImmutableList.builder();
    while (true) {
      Token token = peekToken();
      if (token.type == TokenType.SEMI_COLON) {
        eat(TokenType.SEMI_COLON);
        continue;
      } else {
        if (isClassElementStart(token)) {
        } else {
          return result.build();
        }
      }
      result.add(parseClassElement());
    }
  }

  private boolean isClassElementStart(Token token) {
    switch (token.type) {
      case IDENTIFIER:
      case NUMBER:
      case BIGINT:
      case STAR:
      case STATIC:
      case STRING:
      case OPEN_SQUARE:
        return true;

      default:
        if (Keywords.isKeyword(token.type)) {
          return true;
        }
    }
    return false;
  }

  private ClassOrObjectElementInfo createObjectLiteralElementInfo() {
    return ClassOrObjectElementInfo.createObjectLiteralElementInfo(getTreeStartLocation());
  }

  private ParseTree parseClassElement() {
    if (peek(TokenType.SEMI_COLON)) {
      return parseEmptyStatement();
    }

    if (peekClassStaticInitializerBlock()) {
      return parseClassStaticInitializerBlock();
    }

    ClassOrObjectElementInfo elementInfo =
        ClassOrObjectElementInfo.createClassMemberInfo(
            getTreeStartLocation(), eatStaticIfNotElementName());
    if (peekGetAccessor()) {
      return parseGetAccessor(elementInfo);
    } else if (peekSetAccessor()) {
      return parseSetAccessor(elementInfo);
    } else if (peekAsyncMethod()) {
      return parseAsyncMethod(elementInfo);
    } else {
      return parseClassMemberDeclaration(elementInfo);
    }
  }

  private boolean eatStaticIfNotElementName() {
    // only eat `static` if it being used as a keyword and not
    // a member name.
    if (peek(TokenType.STATIC) && isClassElementStart(peekToken(1))) {
      eat(TokenType.STATIC);
      return true;
    }
    return false;
  }

  private boolean peekAsyncMethod() {
    return peekPredefinedString(ASYNC)
        && !peekImplicitSemiColon(1)
        && (peekPropertyNameOrComputedProp(1)
            || (peek(1, TokenType.STAR) && peekPropertyNameOrComputedProp(2)));
  }

  private boolean peekClassStaticInitializerBlock() {
    return peek(TokenType.STATIC) && peek(1, TokenType.OPEN_CURLY);
  }

  private void parseClassElementName(ClassOrObjectElementInfo elementInfo) {
    if (peekPropertyName(0)) {
      if (peekIdOrKeyword()) {
        elementInfo.setName(eatIdOrKeywordAsId());
        if (elementInfo.getName().isKeyword()) {
          recordFeatureUsed(Feature.KEYWORDS_AS_PROPERTIES);
        }
      } else {
        // { 'str'() {} }
        // { 123() {} }
        // Treat these as if they were computed properties.
        // TODO(b/123769080): Stop making this assumption!
        elementInfo.setNameExpr(parseLiteralExpression());
      }
    } else {
      elementInfo.setNameExpr(parseComputedPropertyName());
    }
  }

  private ParseTree parseFieldDefinition(ClassOrObjectElementInfo elementInfo) {
    ParseTree initializer = null;
    if (peek(TokenType.EQUAL)) {
      initializer = parseInitializer(Expression.NORMAL);
    }

    eatPossiblyImplicitSemiColon();
    if (elementInfo.hasName()) {
      return new FieldDeclarationTree(
          getTreeLocation(elementInfo.start),
          elementInfo.getName(),
          elementInfo.isStatic,
          initializer);
    } else {
      checkState(elementInfo.hasNameExpr());
      return new ComputedPropertyFieldTree(
          getTreeLocation(elementInfo.start),
          elementInfo.getNameExpr(),
          elementInfo.isStatic,
          initializer);
    }
  }

  private ParseTree parseMethodDefinition(
      ClassOrObjectElementInfo elementInfo, boolean isGenerator) {
    FunctionDeclarationTree.Kind kind;
    if (elementInfo.hasNameExpr()) {
      kind = FunctionDeclarationTree.Kind.EXPRESSION;
    } else {
      kind = FunctionDeclarationTree.Kind.MEMBER;
    }

    FunctionDeclarationTree.Builder builder =
        FunctionDeclarationTree.builder(kind)
            .setIsClassMember(elementInfo.isClassMember)
            .setStatic(elementInfo.isStatic);
    if (elementInfo.hasName()) {
      builder.setName(elementInfo.getName());
    }
    parseFunctionTail(builder, isGenerator ? FunctionFlavor.GENERATOR : FunctionFlavor.NORMAL);

    ParseTree function = builder.build(getTreeLocation(elementInfo.start));
    if (kind == FunctionDeclarationTree.Kind.MEMBER) {
      return function;
    } else {
      checkState(elementInfo.hasNameExpr());
      return new ComputedPropertyMethodTree(
          getTreeLocation(elementInfo.start), elementInfo.getNameExpr(), function);
    }
  }

  private ParseTree parseObjectLiteralMethodDeclaration() {
    return parseMethodDeclaration(createObjectLiteralElementInfo());
  }

  private ParseTree parseMethodDeclaration(ClassOrObjectElementInfo elementInfo) {
    boolean isGenerator = eatOpt(TokenType.STAR) != null;
    parseClassElementName(elementInfo);
    return parseMethodDefinition(elementInfo, isGenerator);
  }

  private ParseTree parseClassMemberDeclaration(ClassOrObjectElementInfo elementInfo) {
    boolean isGenerator = eatOpt(TokenType.STAR) != null;
    parseClassElementName(elementInfo);
    if (peekType(0) == TokenType.OPEN_PAREN) {
      return parseMethodDefinition(elementInfo, isGenerator);
    } else {
      return parseFieldDefinition(elementInfo);
    }
  }

  private ParseTree parseObjectLiteralAsyncMethod() {
    return parseAsyncMethod(createObjectLiteralElementInfo());
  }

  private ParseTree parseAsyncMethod(ClassOrObjectElementInfo elementInfo) {
    eatPredefinedString(ASYNC);
    boolean generator = peek(TokenType.STAR);
    if (generator) {
      eat(TokenType.STAR);
    }
    if (peekPropertyName(0)) {
      if (peekIdOrKeyword()) {
        IdentifierToken name = eatIdOrKeywordAsId();
        FunctionDeclarationTree.Builder builder =
            FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.MEMBER)
                .setAsync(true)
                .setGenerator(generator)
                .setIsClassMember(elementInfo.isClassMember)
                .setStatic(elementInfo.isStatic)
                .setName(name);
        parseFunctionTail(
            builder,
            generator ? FunctionFlavor.ASYNCHRONOUS_GENERATOR : FunctionFlavor.ASYNCHRONOUS);

        return builder.build(getTreeLocation(name.getStart()));
      } else {
        // { 'str'() {} }
        // { 123() {} }
        // Treat these as if they were computed properties.
        ParseTree nameExpr = parseLiteralExpression();
        FunctionDeclarationTree.Builder builder =
            FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.EXPRESSION)
                .setAsync(true)
                .setGenerator(generator)
                .setIsClassMember(elementInfo.isClassMember)
                .setStatic(elementInfo.isStatic);
        parseFunctionTail(
            builder,
            generator ? FunctionFlavor.ASYNCHRONOUS_GENERATOR : FunctionFlavor.ASYNCHRONOUS);

        ParseTree function = builder.build(getTreeLocation(nameExpr.getStart()));
        return new ComputedPropertyMethodTree(
            getTreeLocation(nameExpr.getStart()), nameExpr, function);
      }
    } else {
      // expect '[' to start computed property name
      ParseTree nameExpr = parseComputedPropertyName();
      FunctionDeclarationTree.Builder builder =
          FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.EXPRESSION)
              .setAsync(true)
              .setGenerator(generator)
              .setIsClassMember(elementInfo.isClassMember)
              .setStatic(elementInfo.isStatic);
      parseFunctionTail(
          builder, generator ? FunctionFlavor.ASYNCHRONOUS_GENERATOR : FunctionFlavor.ASYNCHRONOUS);

      ParseTree function = builder.build(getTreeLocation(nameExpr.getStart()));
      return new ComputedPropertyMethodTree(
          getTreeLocation(nameExpr.getStart()), nameExpr, function);
    }
  }

  private ParseTree parseClassStaticInitializerBlock() {
    eat(TokenType.STATIC);
    return parseBlock();
  }

  private void parseFunctionTail(
      FunctionDeclarationTree.Builder builder, FunctionFlavor functionFlavor) {
    functionContextStack.addLast(functionFlavor);
    builder
        .setGenerator(functionFlavor.isGenerator)
        .setFormalParameterList(parseFormalParameterList())
        .setFunctionBody(parseFunctionBody());
    functionContextStack.removeLast();
  }

  private ParseTree parseSourceElement() {
    if (peekAsyncFunctionStart()) {
      return parseAsyncFunctionDeclaration();
    }

    if (peekFunction()) {
      return parseFunctionDeclaration();
    }

    if (peekClassDeclaration()) {
      return parseClassDeclaration();
    }

    // Harmony let block scoped bindings. let can only appear in
    // a block, not as a standalone statement: if() let x ... illegal
    if (peek(TokenType.LET)) {
      return parseVariableStatement();
    }
    // const and var are handled inside parseStatement

    return parseStatementStandard();
  }

  private boolean peekSourceElement() {
    return peekFunction() || peekStatementStandard() || peekDeclaration();
  }

  private boolean peekAsyncFunctionStart() {
    return peekPredefinedString(ASYNC) && !peekImplicitSemiColon(1) && peekFunction(1);
  }

  private void eatAsyncFunctionStart() {
    eatPredefinedString(ASYNC);
    eat(TokenType.FUNCTION);
  }

  private boolean peekFunction() {
    return peekFunction(0);
  }

  private boolean peekDeclaration() {
    return peek(TokenType.LET) || peekClassDeclaration();
  }

  private boolean peekFunction(int index) {
    return peek(index, TokenType.FUNCTION);
  }

  // 13 Function Definition
  private ParseTree parseFunctionDeclaration() {
    SourcePosition start = getTreeStartLocation();
    eat(Keywords.FUNCTION.type);
    boolean isGenerator = eatOpt(TokenType.STAR) != null;

    FunctionDeclarationTree.Builder builder =
        FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.DECLARATION).setName(eatId());
    parseFunctionTail(builder, isGenerator ? FunctionFlavor.GENERATOR : FunctionFlavor.NORMAL);
    return builder.build(getTreeLocation(start));
  }

  private ParseTree parseFunctionExpression() {
    SourcePosition start = getTreeStartLocation();
    eat(Keywords.FUNCTION.type);
    boolean isGenerator = eatOpt(TokenType.STAR) != null;

    FunctionDeclarationTree.Builder builder =
        FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.EXPRESSION)
            .setName(eatIdOpt());
    parseFunctionTail(builder, isGenerator ? FunctionFlavor.GENERATOR : FunctionFlavor.NORMAL);

    return builder.build(getTreeLocation(start));
  }

  private ParseTree parseAsyncFunctionDeclaration() {
    SourcePosition start = getTreeStartLocation();
    eatAsyncFunctionStart();

    boolean generator = peek(TokenType.STAR);
    if (generator) {
      eat(TokenType.STAR);
    }

    FunctionDeclarationTree.Builder builder =
        FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.DECLARATION)
            .setName(eatId())
            .setAsync(true);

    parseFunctionTail(
        builder, generator ? FunctionFlavor.ASYNCHRONOUS_GENERATOR : FunctionFlavor.ASYNCHRONOUS);
    return builder.build(getTreeLocation(start));
  }

  private ParseTree parseAsyncFunctionExpression() {
    SourcePosition start = getTreeStartLocation();
    eatAsyncFunctionStart();

    boolean generator = peek(TokenType.STAR);
    if (generator) {
      eat(TokenType.STAR);
    }

    FunctionDeclarationTree.Builder builder =
        FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.EXPRESSION)
            .setName(eatIdOpt())
            .setAsync(true);

    parseFunctionTail(
        builder, generator ? FunctionFlavor.ASYNCHRONOUS_GENERATOR : FunctionFlavor.ASYNCHRONOUS);
    return builder.build(getTreeLocation(start));
  }

  private boolean peekParameter() {
    if (peekId() || peek(TokenType.ELLIPSIS)) {
      return true;
    }
    return peek(TokenType.OPEN_SQUARE) || peek(TokenType.OPEN_CURLY);
  }

  private ParseTree parseParameter() {
    SourcePosition start = getTreeStartLocation();
    ParseTree parameter = null;

    if (peek(TokenType.ELLIPSIS)) {
      parameter = parseIterRest(PatternKind.INITIALIZER);
    } else if (peekId()) {
      parameter = parseIdentifierExpression();
    } else if (peekPatternStart()) {
      parameter = parsePattern(PatternKind.INITIALIZER);
    } else {
      throw new IllegalStateException(
          "parseParameterCalled() without confirming a parameter exists.");
    }

    if (!parameter.isRestParameter() && peek(TokenType.EQUAL)) {
      eat(TokenType.EQUAL);
      ParseTree defaultValue = parseAssignmentExpression();
      parameter = new DefaultParameterTree(getTreeLocation(start), parameter, defaultValue);
    }

    return parameter;
  }

  private FormalParameterListTree parseFormalParameterList() {
    SourcePosition listStart = getTreeStartLocation();
    eat(TokenType.OPEN_PAREN);

    ImmutableList.Builder<ParseTree> result = ImmutableList.builder();
    boolean trailingComma = false;
    ImmutableList.Builder<SourcePosition> commaPositions = ImmutableList.builder();

    while (peekParameter()) {
      result.add(parseParameter());

      if (!peek(TokenType.CLOSE_PAREN)) {
        Token comma = eat(TokenType.COMMA);
        if (comma != null) {
          commaPositions.add(comma.getStart());
        } else {
          // semi-arbitrary comma position in case the code is syntactially invalid & missing one
          commaPositions.add(getTreeEndLocation());
        }
        if (peek(TokenType.CLOSE_PAREN)) {
          if (!config.atLeast8) {
            reportError(comma, "Invalid trailing comma in formal parameter list");
          }
          trailingComma = true;
        }
      }
    }

    eat(TokenType.CLOSE_PAREN);
    return new FormalParameterListTree(
        getTreeLocation(listStart), result.build(), trailingComma, commaPositions.build());
  }

  private FormalParameterListTree parseSetterParameterList() {
    FormalParameterListTree parameterList = parseFormalParameterList();

    if (parameterList.parameters.size() != 1) {
      reportError(
          parameterList,
          "Setter must have exactly 1 parameter, found %d",
          parameterList.parameters.size());
    }

    if (parameterList.parameters.size() >= 1) {
      ParseTree parameter = parameterList.parameters.get(0);
      if (parameter.isRestParameter()) {
        reportError(parameter, "Setter must not have a rest parameter");
      }
    }

    return parameterList;
  }

  private BlockTree parseFunctionBody() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.OPEN_CURLY);
    ImmutableList<ParseTree> result = parseSourceElementList();
    eat(TokenType.CLOSE_CURLY);
    return new BlockTree(getTreeLocation(start), result);
  }

  private ImmutableList<ParseTree> parseSourceElementList() {
    ImmutableList.Builder<ParseTree> result = ImmutableList.builder();

    while (peekSourceElement()) {
      result.add(parseSourceElement());
    }

    return result.build();
  }

  private IterSpreadTree parseIterSpread() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.ELLIPSIS);
    ParseTree operand = parseAssignmentExpression();
    return new IterSpreadTree(getTreeLocation(start), operand);
  }

  // 12 Statements

  /** In V8, all source elements may appear where statements occur in the grammar. */
  private ParseTree parseStatement() {
    return parseSourceElement();
  }

  /** This function reflects the ECMA standard. Most places use peekStatement instead. */
  private ParseTree parseStatementStandard() {
    switch (peekType()) {
      case OPEN_CURLY:
        return parseBlock();
      case CONST:
      case VAR:
        return parseVariableStatement();
      case SEMI_COLON:
        return parseEmptyStatement();
      case IF:
        return parseIfStatement();
      case DO:
        return parseDoWhileStatement();
      case WHILE:
        return parseWhileStatement();
      case FOR:
        return parseForStatement();
      case CONTINUE:
        return parseContinueStatement();
      case BREAK:
        return parseBreakStatement();
      case RETURN:
        return parseReturnStatement();
      case WITH:
        return parseWithStatement();
      case SWITCH:
        return parseSwitchStatement();
      case THROW:
        return parseThrowStatement();
      case TRY:
        return parseTryStatement();
      case DEBUGGER:
        return parseDebuggerStatement();
      default:
        if (peekLabelledStatement()) {
          return parseLabelledStatement();
        }
        return parseExpressionStatement();
    }
  }

  /** In V8 all source elements may appear where statements appear in the grammar. */
  private boolean peekStatement() {
    return peekSourceElement();
  }

  /** This function reflects the ECMA standard. Most places use peekStatement instead. */
  private boolean peekStatementStandard() {
    return switch (peekType()) {
      case OPEN_CURLY,
          VAR,
          CONST,
          SEMI_COLON,
          IF,
          DO,
          WHILE,
          FOR,
          CONTINUE,
          BREAK,
          RETURN,
          WITH,
          SWITCH,
          THROW,
          TRY,
          DEBUGGER,
          YIELD,
          IDENTIFIER,
          TYPE,
          DECLARE,
          MODULE,
          NAMESPACE,
          THIS,
          CLASS,
          SUPER,
          NUMBER,
          BIGINT,
          STRING,
          NO_SUBSTITUTION_TEMPLATE,
          TEMPLATE_HEAD,
          NULL,
          TRUE,
          SLASH, // regular expression literal
          SLASH_EQUAL, // regular expression literal
          FALSE,
          OPEN_SQUARE,
          OPEN_PAREN,
          NEW,
          DELETE,
          VOID,
          TYPEOF,
          PLUS_PLUS,
          MINUS_MINUS,
          PLUS,
          MINUS,
          TILDE,
          BANG,
          IMPORT ->
          true;
      default -> false;
    };
  }

  // 12.1 Block
  private BlockTree parseBlock() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.OPEN_CURLY);
    // Spec says Statement list. However functions are also embedded in the wild.
    ImmutableList<ParseTree> result = parseSourceElementList();
    eat(TokenType.CLOSE_CURLY);
    return new BlockTree(getTreeLocation(start), result);
  }

  private ImmutableList<ParseTree> parseStatementList() {
    ImmutableList.Builder<ParseTree> result = ImmutableList.builder();
    while (peekStatement()) {
      result.add(parseStatement());
    }
    return result.build();
  }

  // 12.2 Variable Statement
  private VariableStatementTree parseVariableStatement() {
    SourcePosition start = getTreeStartLocation();
    VariableDeclarationListTree declarations = parseVariableDeclarationList();
    eatPossiblyImplicitSemiColon();
    return new VariableStatementTree(getTreeLocation(start), declarations);
  }

  private VariableDeclarationListTree parseVariableDeclarationList() {
    return parseVariableDeclarationList(Expression.NORMAL);
  }

  private VariableDeclarationListTree parseVariableDeclarationListNoIn() {
    return parseVariableDeclarationList(Expression.NO_IN);
  }

  private @Nullable VariableDeclarationListTree parseVariableDeclarationList(
      Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    TokenType token = peekType();

    switch (token) {
      case CONST:
      case LET:
      case VAR:
        eat(token);
        break;
      default:
        reportError(peekToken(), "expected declaration");
        return null;
    }

    ImmutableList.Builder<VariableDeclarationTree> declarations = ImmutableList.builder();

    declarations.add(parseVariableDeclaration(token, expressionIn));
    while (peek(TokenType.COMMA)) {
      eat(TokenType.COMMA);
      declarations.add(parseVariableDeclaration(token, expressionIn));
    }
    return new VariableDeclarationListTree(getTreeLocation(start), token, declarations.build());
  }

  private VariableDeclarationTree parseVariableDeclaration(
      final TokenType binding, Expression expressionIn) {

    SourcePosition start = getTreeStartLocation();
    ParseTree lvalue;
    if (peekPatternStart()) {
      lvalue = parsePattern(PatternKind.INITIALIZER);
    } else {
      lvalue = parseIdentifierExpression();
    }

    ParseTree initializer = null;
    if (peek(TokenType.EQUAL)) {
      initializer = parseInitializer(expressionIn);
    } else if (expressionIn != Expression.NO_IN) {
      // NOTE(blickly): this is a bit of a hack, declarations outside of for statements allow "in",
      // and by chance, also must have initializers for const/destructuring. Vanilla for loops
      // also require intializers, but are handled separately in checkVanillaForInitializers
      maybeReportNoInitializer(binding, lvalue);
    }
    return new VariableDeclarationTree(getTreeLocation(start), lvalue, initializer);
  }

  private ParseTree parseInitializer(Expression expressionIn) {
    eat(TokenType.EQUAL);
    return parseAssignment(expressionIn);
  }

  // 12.3 Empty Statement
  private EmptyStatementTree parseEmptyStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.SEMI_COLON);
    return new EmptyStatementTree(getTreeLocation(start));
  }

  // 12.4 Expression Statement
  private ExpressionStatementTree parseExpressionStatement() {
    SourcePosition start = getTreeStartLocation();
    ParseTree expression = parseExpression();
    eatPossiblyImplicitSemiColon();
    return new ExpressionStatementTree(getTreeLocation(start), expression);
  }

  // 12.5 If Statement
  private IfStatementTree parseIfStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.IF);
    eat(TokenType.OPEN_PAREN);
    ParseTree condition = parseExpression();
    eat(TokenType.CLOSE_PAREN);
    ParseTree ifClause = parseStatement();
    ParseTree elseClause = null;
    if (peek(TokenType.ELSE)) {
      eat(TokenType.ELSE);
      elseClause = parseStatement();
    }
    return new IfStatementTree(getTreeLocation(start), condition, ifClause, elseClause);
  }

  // 12.6 Iteration Statements

  // 12.6.1 The do-while Statement
  private ParseTree parseDoWhileStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.DO);
    ParseTree body = parseStatement();
    eat(TokenType.WHILE);
    eat(TokenType.OPEN_PAREN);
    ParseTree condition = parseExpression();
    eat(TokenType.CLOSE_PAREN);
    // The semicolon after the "do-while" is optional.
    if (peek(TokenType.SEMI_COLON)) {
      eat(TokenType.SEMI_COLON);
    }
    return new DoWhileStatementTree(getTreeLocation(start), body, condition);
  }

  // 12.6.2 The while Statement
  private ParseTree parseWhileStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.WHILE);
    eat(TokenType.OPEN_PAREN);
    ParseTree condition = parseExpression();
    eat(TokenType.CLOSE_PAREN);
    ParseTree body = parseStatement();
    return new WhileStatementTree(getTreeLocation(start), condition, body);
  }

  // 12.6.3 The for Statement
  // 12.6.4 The for-in Statement
  // The for-of Statement
  // The for-await-of Statement
  private ParseTree parseForStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.FOR);
    boolean awaited = peekPredefinedString(AWAIT);
    if (awaited) {
      eatPredefinedString(AWAIT);
    }
    eat(TokenType.OPEN_PAREN);
    if (peekVariableDeclarationList()) {
      VariableDeclarationListTree variables = parseVariableDeclarationListNoIn();
      if (peek(TokenType.IN)) {
        if (awaited) {
          reportError("for-await-of is the only allowed asynchronous iteration");
        }
        // for-in: only one declaration allowed
        if (variables.declarations.size() > 1) {
          reportError("for-in statement may not have more than one variable declaration");
        }
        VariableDeclarationTree declaration = variables.declarations.get(0);
        if (declaration.initializer != null) {
          // An initializer is allowed here in ES5 and below, but not in ES6.
          // Warn about it, to encourage people to eliminate it from their code.
          // http://esdiscuss.org/topic/initializer-expression-on-for-in-syntax-subject
          if (config.atLeast6) {
            reportError("for-in statement may not have initializer");
          } else {
            errorReporter.reportWarning(
                declaration.location.start, "for-in statement should not have initializer");
          }
        }

        return parseForInStatement(start, variables);
      } else if (peekPredefinedString(PredefinedName.OF)) {
        // for-of: only one declaration allowed
        if (variables.declarations.size() > 1) {
          if (awaited) {
            reportError("for-await-of statement may not have more than one variable declaration");
          } else {
            reportError("for-of statement may not have more than one variable declaration");
          }
        }
        // for-of: initializer is illegal
        VariableDeclarationTree declaration = variables.declarations.get(0);
        if (declaration.initializer != null) {
          if (awaited) {
            reportError("for-await-of statement may not have initializer");
          } else {
            reportError("for-of statement may not have initializer");
          }
        }

        if (awaited) {
          return parseForAwaitOfStatement(start, variables);
        } else {
          return parseForOfStatement(start, variables);
        }
      } else {
        // "Vanilla" for statement: const/destructuring must have initializer
        checkVanillaForInitializers(variables);
        return parseForStatement(start, variables);
      }
    }

    if (peek(TokenType.SEMI_COLON)) {
      return parseForStatement(start, null);
    }

    ParseTree initializer = parseExpressionNoIn();
    if (peek(TokenType.IN) || peek(TokenType.EQUAL) || peekPredefinedString(PredefinedName.OF)) {
      initializer = transformLeftHandSideExpression(initializer);
      if (!initializer.isValidAssignmentTarget()) {
        reportError("invalid assignment target");
      }
    }

    if (peek(TokenType.IN) || peekPredefinedString(PredefinedName.OF)) {
      if (initializer.type != ParseTreeType.BINARY_OPERATOR
          && initializer.type != ParseTreeType.COMMA_EXPRESSION) {
        if (peek(TokenType.IN)) {
          return parseForInStatement(start, initializer);
        } else {
          // for {await}? ( _ of _ )
          if (awaited) {
            return parseForAwaitOfStatement(start, initializer);
          } else {
            return parseForOfStatement(start, initializer);
          }
        }
      }
    }

    return parseForStatement(start, initializer);
  }

  // The for-of Statement
  // for  (  { let | var }?  identifier  of  expression  )  statement
  private ParseTree parseForOfStatement(SourcePosition start, ParseTree initializer) {
    eatPredefinedString(PredefinedName.OF);
    ParseTree collection = parseExpression();
    eat(TokenType.CLOSE_PAREN);
    ParseTree body = parseStatement();
    return new ForOfStatementTree(getTreeLocation(start), initializer, collection, body);
  }

  private ParseTree parseForAwaitOfStatement(SourcePosition start, ParseTree initializer) {
    // TODO(b/128938049): when top-level await is supported, this shouldn't be a parse error.
    if (functionContextStack.isEmpty() || !functionContextStack.peekLast().isAsynchronous) {
      reportError("'for-await-of' used in a non-async function context");
    }
    eatPredefinedString(PredefinedName.OF);
    ParseTree collection = parseExpression();
    eat(TokenType.CLOSE_PAREN);
    ParseTree body = parseStatement();
    return new ForAwaitOfStatementTree(getTreeLocation(start), initializer, collection, body);
  }

  /** Checks variable declarations in for statements. */
  private void checkVanillaForInitializers(VariableDeclarationListTree variables) {
    for (VariableDeclarationTree declaration : variables.declarations) {
      if (declaration.initializer == null) {
        maybeReportNoInitializer(variables.declarationType, declaration.lvalue);
      }
    }
  }

  /** Reports if declaration requires an initializer, assuming initializer is absent. */
  private void maybeReportNoInitializer(TokenType token, ParseTree lvalue) {
    if (token == TokenType.CONST) {
      reportError("const variables must have an initializer");
    } else if (lvalue.isPattern()) {
      reportError("destructuring must have an initializer");
    }
  }

  private boolean peekVariableDeclarationList() {
    return switch (peekType()) {
      case VAR, CONST, LET -> true;
      default -> false;
    };
  }

  // 12.6.3 The for Statement
  private ParseTree parseForStatement(SourcePosition start, @Nullable ParseTree initializer) {
    if (initializer == null) {
      initializer = new NullTree(new SourceRange(getTreeEndLocation(), getTreeStartLocation()));
    }
    eat(TokenType.SEMI_COLON);

    ParseTree condition;
    if (!peek(TokenType.SEMI_COLON)) {
      condition = parseExpression();
    } else {
      condition = new NullTree(new SourceRange(getTreeEndLocation(), getTreeStartLocation()));
    }
    eat(TokenType.SEMI_COLON);

    ParseTree increment;
    if (!peek(TokenType.CLOSE_PAREN)) {
      increment = parseExpression();
    } else {
      increment = new NullTree(new SourceRange(getTreeEndLocation(), getTreeStartLocation()));
    }
    eat(TokenType.CLOSE_PAREN);
    ParseTree body = parseStatement();
    return new ForStatementTree(getTreeLocation(start), initializer, condition, increment, body);
  }

  // 12.6.4 The for-in Statement
  private ParseTree parseForInStatement(SourcePosition start, ParseTree initializer) {
    eat(TokenType.IN);
    ParseTree collection = parseExpression();
    eat(TokenType.CLOSE_PAREN);
    ParseTree body = parseStatement();
    return new ForInStatementTree(getTreeLocation(start), initializer, collection, body);
  }

  // 12.7 The continue Statement
  private ParseTree parseContinueStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.CONTINUE);
    IdentifierToken name = null;
    if (!peekImplicitSemiColon()) {
      name = eatIdOpt();
    }
    eatPossiblyImplicitSemiColon();
    return new ContinueStatementTree(getTreeLocation(start), name);
  }

  // 12.8 The break Statement
  private ParseTree parseBreakStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.BREAK);
    IdentifierToken name = null;
    if (!peekImplicitSemiColon()) {
      name = eatIdOpt();
    }
    eatPossiblyImplicitSemiColon();
    return new BreakStatementTree(getTreeLocation(start), name);
  }

  // 12.9 The return Statement
  private ParseTree parseReturnStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.RETURN);
    ParseTree expression = null;
    if (!peekImplicitSemiColon()) {
      expression = parseExpression();
    }
    eatPossiblyImplicitSemiColon();
    return new ReturnStatementTree(getTreeLocation(start), expression);
  }

  // 12.10 The with Statement
  private ParseTree parseWithStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.WITH);
    eat(TokenType.OPEN_PAREN);
    ParseTree expression = parseExpression();
    eat(TokenType.CLOSE_PAREN);
    ParseTree body = parseStatement();
    return new WithStatementTree(getTreeLocation(start), expression, body);
  }

  // 12.11 The switch Statement
  private ParseTree parseSwitchStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.SWITCH);
    eat(TokenType.OPEN_PAREN);
    ParseTree expression = parseExpression();
    eat(TokenType.CLOSE_PAREN);
    eat(TokenType.OPEN_CURLY);
    ImmutableList<ParseTree> caseClauses = parseCaseClauses();
    eat(TokenType.CLOSE_CURLY);
    return new SwitchStatementTree(getTreeLocation(start), expression, caseClauses);
  }

  private ImmutableList<ParseTree> parseCaseClauses() {
    boolean foundDefaultClause = false;
    ImmutableList.Builder<ParseTree> result = ImmutableList.builder();

    while (true) {
      SourcePosition start = getTreeStartLocation();
      switch (peekType()) {
        case CASE:
          eat(TokenType.CASE);
          ParseTree expression = parseExpression();
          eat(TokenType.COLON);
          ImmutableList<ParseTree> statements = parseCaseStatementsOpt();
          result.add(new CaseClauseTree(getTreeLocation(start), expression, statements));
          break;
        case DEFAULT:
          if (foundDefaultClause) {
            reportError("Switch statements may have at most one default clause");
          } else {
            foundDefaultClause = true;
          }
          eat(TokenType.DEFAULT);
          eat(TokenType.COLON);
          result.add(new DefaultClauseTree(getTreeLocation(start), parseCaseStatementsOpt()));
          break;
        default:
          return result.build();
      }
    }
  }

  private ImmutableList<ParseTree> parseCaseStatementsOpt() {
    return parseStatementList();
  }

  // 12.12 Labelled Statement
  private ParseTree parseLabelledStatement() {
    SourcePosition start = getTreeStartLocation();
    IdentifierToken name = eatId();
    eat(TokenType.COLON);
    return new LabelledStatementTree(getTreeLocation(start), name, parseStatement());
  }

  private boolean peekLabelledStatement() {
    return peekId() && peek(1, TokenType.COLON);
  }

  // 12.13 Throw Statement
  private ParseTree parseThrowStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.THROW);
    ParseTree value = null;
    if (peekImplicitSemiColon()) {
      reportError("semicolon/newline not allowed after 'throw'");
    } else {
      value = parseExpression();
    }
    eatPossiblyImplicitSemiColon();
    return new ThrowStatementTree(getTreeLocation(start), value);
  }

  // 12.14 Try Statement
  private ParseTree parseTryStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.TRY);
    ParseTree body = parseBlock();
    ParseTree catchBlock = null;
    if (peek(TokenType.CATCH)) {
      catchBlock = parseCatch();
    }
    ParseTree finallyBlock = null;
    if (peek(TokenType.FINALLY)) {
      finallyBlock = parseFinallyBlock();
    }
    if (catchBlock == null && finallyBlock == null) {
      reportError("'catch' or 'finally' expected.");
    }
    return new TryStatementTree(getTreeLocation(start), body, catchBlock, finallyBlock);
  }

  private CatchTree parseCatch() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.CATCH);

    ParseTree exception =
        new EmptyStatementTree(new SourceRange(getTreeEndLocation(), getTreeStartLocation()));

    if (peekToken().type == TokenType.OPEN_PAREN) {
      eat(TokenType.OPEN_PAREN);
      if (peekPatternStart()) {
        exception = parsePattern(PatternKind.INITIALIZER);
      } else {
        exception = parseIdentifierExpression();
      }
      eat(TokenType.CLOSE_PAREN);
    } else {
      recordFeatureUsed(Feature.OPTIONAL_CATCH_BINDING);
    }

    BlockTree catchBody = parseBlock();
    return new CatchTree(getTreeLocation(start), exception, catchBody);
  }

  private FinallyTree parseFinallyBlock() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.FINALLY);
    BlockTree finallyBlock = parseBlock();
    return new FinallyTree(getTreeLocation(start), finallyBlock);
  }

  // 12.15 The Debugger Statement
  private ParseTree parseDebuggerStatement() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.DEBUGGER);
    eatPossiblyImplicitSemiColon();

    return new DebuggerStatementTree(getTreeLocation(start));
  }

  // 11.1 Primary Expressions
  private ParseTree parsePrimaryExpression() {
    return switch (peekType()) {
      case CLASS -> parseClassExpression();
      case SUPER -> parseSuperExpression();
      case THIS -> parseThisExpression();
      case IMPORT -> parseDynamicImportExpression();
      case IDENTIFIER, TYPE, DECLARE, MODULE, NAMESPACE -> parseIdentifierExpression();
      case NUMBER, STRING, BIGINT, TRUE, FALSE, NULL -> parseLiteralExpression();
      case NO_SUBSTITUTION_TEMPLATE, TEMPLATE_HEAD -> parseTemplateLiteral(null);
      case OPEN_SQUARE -> parseArrayInitializer();
      case OPEN_CURLY -> parseObjectLiteral();
      case OPEN_PAREN -> parseCoverParenthesizedExpressionAndArrowParameterList();
      case SLASH, SLASH_EQUAL -> parseRegularExpressionLiteral();
      default -> parseMissingPrimaryExpression();
    };
  }

  private SuperExpressionTree parseSuperExpression() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.SUPER);
    if (peek(TokenType.QUESTION_DOT)) { // super?.() not allowed
      reportError("Optional chaining is forbidden in super?.");
    }
    return new SuperExpressionTree(getTreeLocation(start));
  }

  private ThisExpressionTree parseThisExpression() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.THIS);
    return new ThisExpressionTree(getTreeLocation(start));
  }

  // https://tc39.github.io/proposal-dynamic-import
  private DynamicImportTree parseDynamicImportExpression() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.IMPORT);
    if (peek(TokenType.QUESTION_DOT)) { // import?.() not allowed
      reportError("Optional chaining is forbidden in import?.");
    }
    eat(TokenType.OPEN_PAREN);
    ParseTree argument = parseAssignmentExpression();
    eat(TokenType.CLOSE_PAREN);
    recordFeatureUsed(Feature.DYNAMIC_IMPORT);
    return new DynamicImportTree(getTreeLocation(start), argument);
  }

  private IdentifierExpressionTree parseIdentifierExpression() {
    SourcePosition start = getTreeStartLocation();
    IdentifierToken identifier = eatId();
    return new IdentifierExpressionTree(getTreeLocation(start), identifier);
  }

  private LiteralExpressionTree parseLiteralExpression() {
    SourcePosition start = getTreeStartLocation();
    Token literal = nextLiteralToken();

    if (literal.type == TokenType.STRING
        && ((StringLiteralToken) literal).hasUnescapedUnicodeLineOrParagraphSeparator()) {
      recordFeatureUsed(Feature.UNESCAPED_UNICODE_LINE_OR_PARAGRAPH_SEP);
    }

    if (literal.type == TokenType.NUMBER && literal.toString().contains("_")) {
      recordFeatureUsed(Feature.NUMERIC_SEPARATOR);
    }

    if (literal.type == TokenType.BIGINT) {
      recordFeatureUsed(Feature.BIGINT);
    }

    return new LiteralExpressionTree(getTreeLocation(start), literal);
  }

  /**
   * Constructs a template literal expression tree. "operand" is used to handle the case like
   * "foo`bar`", which is a CallExpression or MemberExpression that calls the function foo() with
   * the template literal as the argument (with extra handling). In this case, operand would be
   * "foo", which is the callsite.
   *
   * <p>We store this operand in the TemplateLiteralExpressionTree and generate a TAGGED_TEMPLATELIT
   * node if it's not null later when transpiling.
   *
   * @param operand A non-null value would represent the callsite
   * @return The template literal expression
   */
  private TemplateLiteralExpressionTree parseTemplateLiteral(@Nullable ParseTree operand) {
    SourcePosition start = operand == null ? getTreeStartLocation() : operand.location.start;
    Token token = nextToken();
    if (!(token instanceof TemplateLiteralToken)) {
      reportError(token, "Unexpected template literal token %s.", token.type.toString());
    }
    boolean isTaggedTemplate = operand != null;
    TemplateLiteralToken templateToken = (TemplateLiteralToken) token;
    if (!isTaggedTemplate) {
      reportTemplateErrorIfPresent(templateToken);
    }
    ImmutableList.Builder<ParseTree> elements = ImmutableList.builder();
    elements.add(new TemplateLiteralPortionTree(templateToken.location, templateToken));
    if (templateToken.type == TokenType.NO_SUBSTITUTION_TEMPLATE) {
      return new TemplateLiteralExpressionTree(getTreeLocation(start), operand, elements.build());
    }

    // `abc${
    ParseTree expression = parseExpression();
    elements.add(new TemplateSubstitutionTree(expression.location, expression));
    while (!errorReporter.hadError()) {
      templateToken = nextTemplateLiteralToken();
      if (templateToken.type == TokenType.ERROR || templateToken.type == TokenType.END_OF_FILE) {
        break;
      }
      if (!isTaggedTemplate) {
        reportTemplateErrorIfPresent(templateToken);
      }
      elements.add(new TemplateLiteralPortionTree(templateToken.location, templateToken));
      if (templateToken.type == TokenType.TEMPLATE_TAIL) {
        break;
      }

      expression = parseExpression();
      elements.add(new TemplateSubstitutionTree(expression.location, expression));
    }

    return new TemplateLiteralExpressionTree(getTreeLocation(start), operand, elements.build());
  }

  private Token nextLiteralToken() {
    return nextToken();
  }

  private ParseTree parseRegularExpressionLiteral() {
    SourcePosition start = getTreeStartLocation();
    LiteralToken literal = nextRegularExpressionLiteralToken();
    recordFeatureUsed(Feature.REGEXP_SYNTAX);
    return new LiteralExpressionTree(getTreeLocation(start), literal);
  }

  private ParseTree parseArrayInitializer() {
    if (peekType(1) == TokenType.FOR) {
      return parseArrayComprehension();
    } else {
      return parseArrayLiteral();
    }
  }

  private ParseTree parseGeneratorComprehension() {
    return parseComprehension(
        ComprehensionTree.ComprehensionType.GENERATOR, TokenType.OPEN_PAREN, TokenType.CLOSE_PAREN);
  }

  private ParseTree parseArrayComprehension() {
    return parseComprehension(
        ComprehensionTree.ComprehensionType.ARRAY, TokenType.OPEN_SQUARE, TokenType.CLOSE_SQUARE);
  }

  private ParseTree parseComprehension(
      ComprehensionTree.ComprehensionType type, TokenType startToken, TokenType endToken) {
    SourcePosition start = getTreeStartLocation();
    eat(startToken);

    ImmutableList.Builder<ParseTree> children = ImmutableList.builder();
    while (peek(TokenType.FOR) || peek(TokenType.IF)) {
      if (peek(TokenType.FOR)) {
        children.add(parseComprehensionFor());
      } else {
        children.add(parseComprehensionIf());
      }
    }

    ParseTree tailExpression = parseAssignmentExpression();
    eat(endToken);

    return new ComprehensionTree(getTreeLocation(start), type, children.build(), tailExpression);
  }

  private ParseTree parseComprehensionFor() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.FOR);
    eat(TokenType.OPEN_PAREN);

    ParseTree initializer;
    if (peekId()) {
      initializer = parseIdentifierExpression();
    } else {
      initializer = parsePattern(PatternKind.ANY);
    }

    eatPredefinedString(PredefinedName.OF);
    ParseTree collection = parseAssignmentExpression();
    eat(TokenType.CLOSE_PAREN);
    return new ComprehensionForTree(getTreeLocation(start), initializer, collection);
  }

  private ParseTree parseComprehensionIf() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.IF);
    eat(TokenType.OPEN_PAREN);
    ParseTree initializer = parseAssignmentExpression();
    eat(TokenType.CLOSE_PAREN);
    return new ComprehensionIfTree(getTreeLocation(start), initializer);
  }

  // 11.1.4 Array Literal Expression
  private ParseTree parseArrayLiteral() {
    // ArrayLiteral :
    //   [ Elisionopt ]
    //   [ ElementList ]
    //   [ ElementList , Elisionopt ]
    //
    // ElementList :
    //   Elisionopt AssignmentOrSpreadExpression
    //   ElementList , Elisionopt AssignmentOrSpreadExpression
    //
    // Elision :
    //   ,
    //   Elision ,

    SourcePosition start = getTreeStartLocation();
    ImmutableList.Builder<ParseTree> elements = ImmutableList.builder();

    eat(TokenType.OPEN_SQUARE);
    Token trailingCommaToken = null;
    while (peek(TokenType.COMMA) || peek(TokenType.ELLIPSIS) || peekAssignmentExpression()) {
      trailingCommaToken = null;
      if (peek(TokenType.COMMA)) {
        SourcePosition commaStart = getTreeStartLocation();
        trailingCommaToken = eat(TokenType.COMMA);
        // Consider the empty element to start & end immediately before the comma token.
        elements.add(new NullTree(new SourceRange(commaStart, commaStart)));

      } else {
        if (peek(TokenType.ELLIPSIS)) {
          recordFeatureUsed(Feature.SPREAD_EXPRESSIONS);
          elements.add(parseIterSpread());
        } else {
          elements.add(parseAssignmentExpression());
        }
        if (!peek(TokenType.CLOSE_SQUARE)) {
          trailingCommaToken = eat(TokenType.COMMA);
        }
      }
    }
    eat(TokenType.CLOSE_SQUARE);

    maybeReportTrailingComma(trailingCommaToken);

    return new ArrayLiteralExpressionTree(
        getTreeLocation(start), elements.build(), trailingCommaToken != null);
  }

  // 11.1.4 Object Literal Expression
  private ParseTree parseObjectLiteral() {
    SourcePosition start = getTreeStartLocation();
    ImmutableList.Builder<ParseTree> result = ImmutableList.builder();

    eat(TokenType.OPEN_CURLY);
    Token commaToken = null;
    while (peek(TokenType.ELLIPSIS) || peekPropertyNameOrComputedProp(0) || peek(TokenType.STAR)) {
      result.add(parseObjectLiteralPropertyAssignment());
      commaToken = eatOpt(TokenType.COMMA);
      if (commaToken == null) {
        break;
      }
    }
    eat(TokenType.CLOSE_CURLY);

    maybeReportTrailingComma(commaToken);

    return new ObjectLiteralExpressionTree(
        getTreeLocation(start), result.build(), commaToken != null);
  }

  void maybeReportTrailingComma(Token commaToken) {
    if (commaToken != null) {
      recordFeatureUsed(Feature.TRAILING_COMMA);
      if (config.warnTrailingCommas) {
        // In ES3 mode warn about trailing commas which aren't accepted by
        // older browsers (such as IE8).
        errorReporter.reportWarning(
            commaToken.location.start,
            "Trailing comma is not legal in an ECMA-262 object initializer");
      }
    }
  }

  private boolean peekPropertyNameOrComputedProp(int tokenIndex) {
    return peekPropertyName(tokenIndex) || peekType(tokenIndex) == TokenType.OPEN_SQUARE;
  }

  private boolean peekPropertyName(int tokenIndex) {
    TokenType type = peekType(tokenIndex);
    return switch (type) {
      case IDENTIFIER, STRING, NUMBER, BIGINT -> true;
      default -> Keywords.isKeyword(type);
    };
  }

  private ParseTree parseObjectLiteralPropertyAssignment() {
    TokenType type = peekType();
    if (type == TokenType.STAR) {
      return parseObjectLiteralPropertyAssignmentGenerator();
    } else if (type == TokenType.ELLIPSIS) {
      recordFeatureUsed(Feature.OBJECT_LITERALS_WITH_SPREAD);
      SourcePosition start = getTreeStartLocation();
      eat(TokenType.ELLIPSIS);
      ParseTree operand = parseAssignmentExpression();
      return new ObjectSpreadTree(getTreeLocation(start), operand);
    } else if (type == TokenType.STRING
        || type == TokenType.NUMBER
        || type == TokenType.BIGINT
        || type == TokenType.IDENTIFIER
        || Keywords.isKeyword(type)) {
      if (peekGetAccessor()) {
        return parseObjectLiteralGetAccessor();
      } else if (peekSetAccessor()) {
        return parseObjectLiteralSetAccessor();
      } else if (peekAsyncMethod()) {
        return parseObjectLiteralAsyncMethod();
      } else if (peekType(1) == TokenType.OPEN_PAREN) {
        return parseObjectLiteralMethodDeclaration();
      } else {
        return parseObjectLiteralPropertyNameAssignment();
      }
    } else if (type == TokenType.OPEN_SQUARE) {
      SourcePosition start = getTreeStartLocation();
      ParseTree name = parseComputedPropertyName();

      if (peek(TokenType.COLON)) {
        eat(TokenType.COLON);
        ParseTree value = parseAssignmentExpression();
        return new ComputedPropertyDefinitionTree(getTreeLocation(start), name, value);
      } else {
        FunctionDeclarationTree.Builder builder =
            FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.EXPRESSION);
        parseFunctionTail(builder, FunctionFlavor.NORMAL);
        ParseTree value = builder.build(getTreeLocation(start));
        return new ComputedPropertyMethodTree(getTreeLocation(start), name, value);
      }
    } else {
      throw new RuntimeException("unreachable");
    }
  }

  private ParseTree parseObjectLiteralPropertyAssignmentGenerator() {
    TokenType type = peekType(1);
    if (type == TokenType.STRING
        || type == TokenType.NUMBER
        || type == TokenType.IDENTIFIER
        || Keywords.isKeyword(type)) {
      // parseMethodDeclaration will consume the '*'.
      return parseObjectLiteralMethodDeclaration();
    } else {
      SourcePosition start = getTreeStartLocation();
      eat(TokenType.STAR);

      ParseTree name = parseComputedPropertyName();
      FunctionDeclarationTree.Builder builder =
          FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.EXPRESSION);

      parseFunctionTail(builder, FunctionFlavor.GENERATOR);
      ParseTree value = builder.build(getTreeLocation(start));
      return new ComputedPropertyMethodTree(getTreeLocation(start), name, value);
    }
  }

  private ParseTree parseComputedPropertyName() {

    eat(TokenType.OPEN_SQUARE);
    ParseTree assign = parseAssignmentExpression();
    eat(TokenType.CLOSE_SQUARE);
    return assign;
  }

  private boolean peekGetAccessor() {
    return peekPredefinedString(PredefinedName.GET) && peekPropertyNameOrComputedProp(1);
  }

  private boolean peekPredefinedString(String string) {
    return peekPredefinedString(0, string);
  }

  private @Nullable Token eatPredefinedString(String string) {
    Token token = eatId();
    if (token == null || !token.asIdentifier().valueEquals(string)) {
      reportExpectedError(token, string);
      return null;
    }
    return token;
  }

  private boolean peekPredefinedString(int index, String string) {
    return peek(index, TokenType.IDENTIFIER)
        && ((IdentifierToken) peekToken(index)).valueEquals(string);
  }

  private ParseTree parseObjectLiteralGetAccessor() {
    return parseGetAccessor(createObjectLiteralElementInfo());
  }

  private ParseTree parseGetAccessor(ClassOrObjectElementInfo elementInfo) {
    eatPredefinedString(PredefinedName.GET);

    if (peekPropertyName(0)) {
      Token propertyName = eatObjectLiteralPropertyName();
      eat(TokenType.OPEN_PAREN);
      eat(TokenType.CLOSE_PAREN);
      BlockTree body = parseFunctionBody();
      recordFeatureUsed(Feature.GETTER);
      return new GetAccessorTree(
          getTreeLocation(elementInfo.start),
          propertyName,
          elementInfo.isClassMember,
          elementInfo.isStatic,
          body);
    } else {
      ParseTree property = parseComputedPropertyName();
      eat(TokenType.OPEN_PAREN);
      eat(TokenType.CLOSE_PAREN);
      BlockTree body = parseFunctionBody();
      recordFeatureUsed(Feature.GETTER);
      return new ComputedPropertyGetterTree(
          getTreeLocation(elementInfo.start), property, elementInfo.isStatic, body);
    }
  }

  private boolean peekSetAccessor() {
    return peekPredefinedString(PredefinedName.SET) && peekPropertyNameOrComputedProp(1);
  }

  private ParseTree parseObjectLiteralSetAccessor() {
    return parseSetAccessor(createObjectLiteralElementInfo());
  }

  private ParseTree parseSetAccessor(ClassOrObjectElementInfo elementInfo) {
    eatPredefinedString(PredefinedName.SET);
    if (peekPropertyName(0)) {
      Token propertyName = eatObjectLiteralPropertyName();
      FormalParameterListTree parameter = parseSetterParameterList();

      BlockTree body = parseFunctionBody();

      recordFeatureUsed(Feature.SETTER);
      return new SetAccessorTree(
          getTreeLocation(elementInfo.start),
          propertyName,
          elementInfo.isClassMember,
          elementInfo.isStatic,
          parameter,
          body);
    } else {
      ParseTree property = parseComputedPropertyName();
      FormalParameterListTree parameter = parseSetterParameterList();
      BlockTree body = parseFunctionBody();

      recordFeatureUsed(Feature.SETTER);
      return new ComputedPropertySetterTree(
          getTreeLocation(elementInfo.start), property, elementInfo.isStatic, parameter, body);
    }
  }

  private ParseTree parseObjectLiteralPropertyNameAssignment() {
    SourcePosition start = getTreeStartLocation();
    Token name = eatObjectLiteralPropertyName();
    Token colon = eatOpt(TokenType.COLON);
    if (colon == null) {
      if (name.type != TokenType.IDENTIFIER) {
        reportExpectedError(peekToken(), TokenType.COLON);
      } else if (name.asIdentifier().isKeyword()) {
        reportError(name, "Cannot use keyword in short object literal");
      } else if (peek(TokenType.EQUAL)) {
        IdentifierExpressionTree idTree =
            new IdentifierExpressionTree(getTreeLocation(start), (IdentifierToken) name);
        eat(TokenType.EQUAL);
        ParseTree defaultValue = parseAssignmentExpression();
        return new DefaultParameterTree(getTreeLocation(start), idTree, defaultValue);
      }
    }
    ParseTree value = colon == null ? null : parseAssignmentExpression();
    return new PropertyNameAssignmentTree(getTreeLocation(start), name, value);
  }

  // 12.2 Primary Expression
  //   CoverParenthesizedExpressionAndArrowParameterList ::=
  //     ( Expression )
  //     ( Expression, )
  //     ( )
  //     ( ... BindingIdentifier )
  //     ( Expression , ... BindingIdentifier )
  private ParseTree parseCoverParenthesizedExpressionAndArrowParameterList() {
    if (peekType(1) == TokenType.FOR) {
      return parseGeneratorComprehension();
    }

    SourcePosition start = getTreeStartLocation();
    eat(TokenType.OPEN_PAREN);
    // Case ( )
    if (peek(TokenType.CLOSE_PAREN)) {
      eat(TokenType.CLOSE_PAREN);
      if (peek(TokenType.ARROW)) {
        return new FormalParameterListTree(
            getTreeLocation(start),
            ImmutableList.<ParseTree>of(),
            /* hasTrailingComma= */ false,
            ImmutableList.<SourcePosition>of());
      } else {
        reportError("invalid parenthesized expression");
        return new MissingPrimaryExpressionTree(getTreeLocation(start));
      }
    }
    // Case ( ... BindingIdentifier )
    if (peek(TokenType.ELLIPSIS)) {
      ImmutableList<ParseTree> params = ImmutableList.of(parseParameter());
      eat(TokenType.CLOSE_PAREN);
      if (peek(TokenType.ARROW)) {
        return new FormalParameterListTree(
            getTreeLocation(start),
            params,
            /* hasTrailingComma= */ false,
            ImmutableList.<SourcePosition>of());
      } else {
        reportError("invalid parenthesized expression");
        return new MissingPrimaryExpressionTree(getTreeLocation(start));
      }
    }
    // For either of the three remaining cases:
    //     ( Expression )
    //     ( Expression, )
    //     ( Expression, ...BindingIdentifier )
    // we can parse as an expression.
    ParseTree result = parseExpression();
    // If it follows with a comma, we must be in either of two cases
    //     ( Expression, )
    //     ( Expression, ...BindingIdentifier )
    // case.
    if (peek(TokenType.COMMA)) {
      if (peek(1, TokenType.CLOSE_PAREN)) {
        // Create the formal parameter list here so we can record
        // the trailing comma
        resetScanner(start);
        // If we fail to parse as an ArrowFunction parameter list then
        // parseFormalParameterList will take care of reporting errors.
        return parseFormalParameterList();
      } else {
        eat(TokenType.COMMA);
        // Since we already parsed as an expression, we will guaranteed reparse this expression
        // as an arrow function parameter list, but just leave it as a comma expression for now.
        result =
            new CommaExpressionTree(
                getTreeLocation(start), ImmutableList.of(result, parseParameter()));
      }
    }
    eat(TokenType.CLOSE_PAREN);
    return new ParenExpressionTree(getTreeLocation(start), result);
  }

  private ParseTree parseMissingPrimaryExpression() {
    SourcePosition start = getTreeStartLocation();
    nextToken();
    reportError("primary expression expected");
    return new MissingPrimaryExpressionTree(getTreeLocation(start));
  }

  /** Differentiates between parsing for 'In' vs. 'NoIn' Variants of expression grammars. */
  private enum Expression {
    NO_IN,
    NORMAL,
  }

  // 11.14 Expressions
  private ParseTree parseExpressionNoIn() {
    return parse(Expression.NO_IN);
  }

  private ParseTree parseExpression() {
    return parse(Expression.NORMAL);
  }

  private boolean peekExpression() {
    return switch (peekType()) {
      case BANG,
          CLASS,
          DELETE,
          FALSE,
          FUNCTION,
          IDENTIFIER,
          TYPE,
          DECLARE,
          MODULE,
          NAMESPACE,
          MINUS,
          MINUS_MINUS,
          NEW,
          NULL,
          NUMBER,
          BIGINT,
          OPEN_CURLY,
          OPEN_PAREN,
          OPEN_SQUARE,
          PLUS,
          PLUS_PLUS,
          SLASH, // regular expression literal
          SLASH_EQUAL, // regular expression literal
          STRING,
          NO_SUBSTITUTION_TEMPLATE,
          TEMPLATE_HEAD,
          SUPER,
          THIS,
          TILDE,
          TRUE,
          TYPEOF,
          VOID,
          YIELD ->
          true;
      case IMPORT -> peekImportCall() || peekImportDot();
      default -> false;
    };
  }

  private ParseTree parse(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree result = parseAssignment(expressionIn);
    if (peek(TokenType.COMMA) && !peek(1, TokenType.ELLIPSIS) && !peek(1, TokenType.CLOSE_PAREN)) {
      ImmutableList.Builder<ParseTree> exprs = ImmutableList.builder();
      exprs.add(result);
      while (peek(TokenType.COMMA)
          && !peek(1, TokenType.ELLIPSIS)
          && !peek(1, TokenType.CLOSE_PAREN)) {
        eat(TokenType.COMMA);
        exprs.add(parseAssignment(expressionIn));
      }
      return new CommaExpressionTree(getTreeLocation(start), exprs.build());
    }
    return result;
  }

  // 12.14 Assignment operators
  private ParseTree parseAssignmentExpression() {
    return parseAssignment(Expression.NORMAL);
  }

  private boolean peekAssignmentExpression() {
    return peekExpression();
  }

  private ParseTree parseAssignment(Expression expressionIn) {
    if (peek(TokenType.YIELD) && inGeneratorContext()) {
      return parseYield(expressionIn);
    }

    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseConditional(expressionIn);

    if (isStartOfAsyncArrowFunction(left)) {
      // re-evaluate as an async arrow function.
      resetScanner(left);
      return parseAsyncArrowFunction(expressionIn);
    }
    if (peek(TokenType.ARROW)) {
      return completeAssignmentExpressionParseAtArrow(left, expressionIn);
    }

    if (peekAssignmentOperator()) {
      if (!peek(TokenType.EQUAL)) {
        // not the vanilla assignment operator `=`, but a special equals operator (`+=`, `-=`,
        // `**=`, etc)
        if (!left.isValidNonVanillaAssignmentTarget()) {
          reportError("invalid assignment target");
          return new MissingPrimaryExpressionTree(getTreeLocation(getTreeStartLocation()));
        }
      }
      left = transformLeftHandSideExpression(left);
      if (!left.isValidAssignmentTarget()) {
        reportError("invalid assignment target");
        return new MissingPrimaryExpressionTree(getTreeLocation(getTreeStartLocation()));
      }
      Token operator = nextToken();
      ParseTree right = parseAssignment(expressionIn);
      return new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  private boolean isStartOfAsyncArrowFunction(ParseTree partialExpression) {
    if (partialExpression.type == ParseTreeType.IDENTIFIER_EXPRESSION) {
      final IdentifierToken identifierToken =
          partialExpression.asIdentifierExpression().identifierToken;
      // partialExpression is `async`
      // followed by `[no newline] bindingIdentifier [no newline] =>`
      return identifierToken.valueEquals(ASYNC)
          && !peekImplicitSemiColon(0)
          && peekId()
          && !peekImplicitSemiColon(1)
          && peek(1, TokenType.ARROW);
    } else if (partialExpression.type == ParseTreeType.CALL_EXPRESSION) {
      final CallExpressionTree callExpression = partialExpression.asCallExpression();
      ParseTree callee = callExpression.operand;
      ParseTree arguments = callExpression.arguments;
      // partialExpression is `async [no newline] (parameters)`
      // followed by `[no newline] =>`
      return callee.type == ParseTreeType.IDENTIFIER_EXPRESSION
          && callee.asIdentifierExpression().identifierToken.valueEquals(ASYNC)
          && callee.location.end.line == arguments.location.start.line
          && !peekImplicitSemiColon()
          && peek(TokenType.ARROW);
    } else {
      return false;
    }
  }

  private ParseTree completeAssignmentExpressionParseAtArrow(
      ParseTree leftOfArrow, Expression expressionIn) {
    if (leftOfArrow.type == ParseTreeType.CALL_EXPRESSION) {
      //   ... someAssignmentExpression // implicit semicolon
      //   (args) =>
      return completeAssignmentExpressionParseAtArrow(leftOfArrow.asCallExpression());
    } else {
      return completeArrowFunctionParseAtArrow(leftOfArrow, expressionIn);
    }
  }

  private ParseTree completeArrowFunctionParseAtArrow(
      ParseTree leftOfArrow, Expression expressionIn) {
    FormalParameterListTree arrowFormalParameters = transformToArrowFormalParameters(leftOfArrow);
    if (peekImplicitSemiColon()) {
      reportError("No newline allowed before '=>'");
    }
    eat(TokenType.ARROW);
    ParseTree arrowFunctionBody = parseArrowFunctionBody(expressionIn, FunctionFlavor.NORMAL);

    FunctionDeclarationTree.Builder builder =
        FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.ARROW)
            .setFormalParameterList(arrowFormalParameters)
            .setFunctionBody(arrowFunctionBody);
    return builder.build(getTreeLocation(arrowFormalParameters.location.start));
  }

  private FormalParameterListTree transformToArrowFormalParameters(ParseTree leftOfArrow) {
    FormalParameterListTree arrowParameterList;
    switch (leftOfArrow.type) {
      case FORMAL_PARAMETER_LIST:
        arrowParameterList = leftOfArrow.asFormalParameterList();
        break;
      case IDENTIFIER_EXPRESSION:
        // e.g. x => x + 1
        arrowParameterList =
            new FormalParameterListTree(
                leftOfArrow.location,
                ImmutableList.<ParseTree>of(leftOfArrow),
                /* hasTrailingComma= */ false,
                ImmutableList.<SourcePosition>of());
        break;
      case ARGUMENT_LIST:
      case PAREN_EXPRESSION:
        // e.g. (x) => x + 1
        resetScanner(leftOfArrow);
        // If we fail to parse as an ArrowFunction parameter list then
        // parseFormalParameterList will take care of reporting errors.
        arrowParameterList = parseFormalParameterList();
        break;
      default:
        reportError(leftOfArrow, "invalid arrow function parameters");
        arrowParameterList = newEmptyFormalParameterList(leftOfArrow.location);
    }
    return arrowParameterList;
  }

  private ParseTree completeAssignmentExpressionParseAtArrow(CallExpressionTree callExpression) {
    ParseTree operand = callExpression.operand;
    ParseTree arguments = callExpression.arguments;
    ParseTree result;
    if (operand.location.end.line < arguments.location.start.line) {
      // break at the implicit semicolon
      // Example:
      // foo.bar // operand and implicit semicolon
      // () => { doSomething; };
      resetScannerAfter(operand);
      result = operand;
    } else {
      reportError("'=>' unexpected");
      result = callExpression;
    }
    return result;
  }

  private ParseTree parseAsyncArrowFunction(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    eatPredefinedString(ASYNC);
    if (peekImplicitSemiColon()) {
      reportError("No newline allowed between `async` and arrow function parameter list");
    }
    FormalParameterListTree arrowParameterList = null;
    if (peek(TokenType.OPEN_PAREN)) {
      // async (...) =>
      arrowParameterList = parseFormalParameterList();
    } else {
      // async arg =>
      final IdentifierExpressionTree singleParameter = parseIdentifierExpression();
      arrowParameterList =
          new FormalParameterListTree(
              singleParameter.location,
              ImmutableList.<ParseTree>of(singleParameter),
              /* hasTrailingComma= */ false,
              ImmutableList.<SourcePosition>of());
    }
    if (peekImplicitSemiColon()) {
      reportError("No newline allowed before '=>'");
    }
    eat(TokenType.ARROW);
    ParseTree arrowFunctionBody = parseArrowFunctionBody(expressionIn, FunctionFlavor.ASYNCHRONOUS);

    FunctionDeclarationTree.Builder builder =
        FunctionDeclarationTree.builder(FunctionDeclarationTree.Kind.ARROW)
            .setAsync(true)
            .setFormalParameterList(arrowParameterList)
            .setFunctionBody(arrowFunctionBody);
    return builder.build(getTreeLocation(start));
  }

  private ParseTree parseArrowFunctionBody(Expression expressionIn, FunctionFlavor functionFlavor) {
    functionContextStack.addLast(functionFlavor);
    ParseTree arrowFunctionBody;
    if (peek(TokenType.OPEN_CURLY)) {
      arrowFunctionBody = parseFunctionBody();
    } else {
      arrowFunctionBody = parseAssignment(expressionIn);
    }
    functionContextStack.removeLast();
    return arrowFunctionBody;
  }

  private static FormalParameterListTree newEmptyFormalParameterList(SourceRange location) {
    return new FormalParameterListTree(
        location,
        ImmutableList.<ParseTree>of(),
        /* hasTrailingComma= */ false,
        ImmutableList.<SourcePosition>of());
  }

  /**
   * Transforms a LeftHandSideExpression into a LeftHandSidePattern if possible. This returns the
   * transformed tree if it parses as a LeftHandSidePattern, otherwise it returns the original tree.
   */
  private ParseTree transformLeftHandSideExpression(ParseTree tree) {
    return switch (tree.type) {
      case ARRAY_LITERAL_EXPRESSION, OBJECT_LITERAL_EXPRESSION -> {
        resetScanner(tree);
        // If we fail to parse as an LeftHandSidePattern then
        // parseLeftHandSidePattern will take care reporting errors.
        yield parseLeftHandSidePattern();
      }
      default -> tree;
    };
  }

  private ParseTree parseLeftHandSidePattern() {
    return parsePattern(PatternKind.ANY);
  }

  private void resetScanner(SourcePosition start) {
    // TODO(bradfordcsmith): lastSourcePosition should really point to the end of the last token
    //     before the tree to correctly detect implicit semicolons, but it doesn't matter for the
    //     current use case.
    lastSourcePosition = start;
    scanner.setPosition(lastSourcePosition);
  }

  private void resetScanner(ParseTree tree) {
    scanner.setPosition(tree.location.start);
  }

  private void resetScannerAfter(ParseTree parseTree) {
    lastSourcePosition = parseTree.location.end;
    // NOTE: The "end" position for a parseTree actually points to the first character after the
    //     last token in the tree, so this is not an off-by-one error.
    scanner.setPosition(lastSourcePosition);
  }

  private boolean peekAssignmentOperator() {
    return switch (peekType()) {
      case EQUAL,
          STAR_EQUAL,
          STAR_STAR_EQUAL,
          SLASH_EQUAL,
          PERCENT_EQUAL,
          PLUS_EQUAL,
          MINUS_EQUAL,
          LEFT_SHIFT_EQUAL,
          RIGHT_SHIFT_EQUAL,
          UNSIGNED_RIGHT_SHIFT_EQUAL,
          AMPERSAND_EQUAL,
          CARET_EQUAL,
          BAR_EQUAL,
          OR_EQUAL,
          AND_EQUAL,
          QUESTION_QUESTION_EQUAL ->
          true;
      default -> false;
    };
  }

  private boolean inGeneratorContext() {
    // disallow yield outside of generators
    return functionContextStack.peekLast().isGenerator;
  }

  // yield [no line terminator] (*)? AssignExpression
  // https://people.mozilla.org/~jorendorff/es6-draft.html#sec-generator-function-definitions-runtime-semantics-evaluation
  private ParseTree parseYield(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.YIELD);
    boolean isYieldAll = false;
    ParseTree expression = null;
    if (!peekImplicitSemiColon()) {
      isYieldAll = eatOpt(TokenType.STAR) != null;
      if (peekAssignmentExpression()) {
        expression = parseAssignment(expressionIn);
      } else if (isYieldAll) {
        reportError("yield* requires an expression");
      }
    }
    return new YieldExpressionTree(getTreeLocation(start), isYieldAll, expression);
  }

  // 11.12 Conditional Expression
  private ParseTree parseConditional(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree condition = parseShortCircuit(expressionIn);
    if (peek(TokenType.QUESTION)) {
      eat(TokenType.QUESTION);
      ParseTree left = parseAssignment(expressionIn);
      eat(TokenType.COLON);
      ParseTree right = parseAssignment(expressionIn);
      return new ConditionalExpressionTree(getTreeLocation(start), condition, left, right);
    }
    return condition;
  }

  private ParseTree parseShortCircuit(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseLogicalOR(expressionIn);
    if (peek(TokenType.QUESTION_QUESTION)) {
      if (left.type == ParseTreeType.BINARY_OPERATOR) {
        BinaryOperatorTree binaryTree = left.asBinaryOperator();
        if (binaryTree.operator.type == TokenType.AND || binaryTree.operator.type == TokenType.OR) {
          reportError("Logical OR and logical AND require parentheses when used with '??'");
        }
      }
      return parseNullishCoalesce(expressionIn, left, start);
    } else {
      return left;
    }
  }

  private ParseTree parseNullishCoalesce(
      Expression expressionIn, ParseTree left, SourcePosition start) {
    while (peek(TokenType.QUESTION_QUESTION)) {
      Token operator = eat(TokenType.QUESTION_QUESTION);
      ParseTree right = parseBitwiseOR(expressionIn);
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    if (peek(TokenType.AND) || peek(TokenType.OR)) {
      reportError("Logical OR and logical AND require parentheses when used with '??'");
    }
    return left;
  }

  // 11.11 Logical OR
  private ParseTree parseLogicalOR(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseLogicalAND(expressionIn);
    while (peek(TokenType.OR)) {
      Token operator = eat(TokenType.OR);
      ParseTree right = parseLogicalAND(expressionIn);
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  // 11.11 Logical AND
  private ParseTree parseLogicalAND(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseBitwiseOR(expressionIn);
    while (peek(TokenType.AND)) {
      Token operator = eat(TokenType.AND);
      ParseTree right = parseBitwiseOR(expressionIn);
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  // 11.10 Bitwise OR
  private ParseTree parseBitwiseOR(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseBitwiseXOR(expressionIn);
    while (peek(TokenType.BAR)) {
      Token operator = eat(TokenType.BAR);
      ParseTree right = parseBitwiseXOR(expressionIn);
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  // 11.10 Bitwise XOR
  private ParseTree parseBitwiseXOR(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseBitwiseAND(expressionIn);
    while (peek(TokenType.CARET)) {
      Token operator = eat(TokenType.CARET);
      ParseTree right = parseBitwiseAND(expressionIn);
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  // 11.10 Bitwise AND
  private ParseTree parseBitwiseAND(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseEquality(expressionIn);
    while (peek(TokenType.AMPERSAND)) {
      Token operator = eat(TokenType.AMPERSAND);
      ParseTree right = parseEquality(expressionIn);
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  // 11.9 Equality Expression
  private ParseTree parseEquality(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseRelational(expressionIn);
    while (peekEqualityOperator()) {
      Token operator = nextToken();
      ParseTree right = parseRelational(expressionIn);
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  private boolean peekEqualityOperator() {
    return switch (peekType()) {
      case EQUAL_EQUAL, NOT_EQUAL, EQUAL_EQUAL_EQUAL, NOT_EQUAL_EQUAL -> true;
      default -> false;
    };
  }

  // 11.8 Relational
  private ParseTree parseRelational(Expression expressionIn) {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseShiftExpression();
    while (peekRelationalOperator(expressionIn)) {
      Token operator = nextToken();
      ParseTree right = parseShiftExpression();
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  private boolean peekRelationalOperator(Expression expressionIn) {
    return switch (peekType()) {
      case OPEN_ANGLE, CLOSE_ANGLE, GREATER_EQUAL, LESS_EQUAL, INSTANCEOF -> true;
      case IN -> expressionIn == Expression.NORMAL;
      default -> false;
    };
  }

  // 11.7 Shift Expression
  private ParseTree parseShiftExpression() {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseAdditiveExpression();
    while (peekShiftOperator()) {
      Token operator = nextToken();
      ParseTree right = parseAdditiveExpression();
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  private boolean peekShiftOperator() {
    return switch (peekType()) {
      case LEFT_SHIFT, RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT -> true;
      default -> false;
    };
  }

  // 11.6 Additive Expression
  private ParseTree parseAdditiveExpression() {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseMultiplicativeExpression();
    while (peekAdditiveOperator()) {
      Token operator = nextToken();
      ParseTree right = parseMultiplicativeExpression();
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  private boolean peekAdditiveOperator() {
    return switch (peekType()) {
      case PLUS, MINUS -> true;
      default -> false;
    };
  }

  // 11.5 Multiplicative Expression
  private ParseTree parseMultiplicativeExpression() {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseExponentiationExpression();
    while (peekMultiplicativeOperator()) {
      Token operator = nextToken();
      ParseTree right = parseExponentiationExpression();
      left = new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    }
    return left;
  }

  private boolean peekMultiplicativeOperator() {
    return switch (peekType()) {
      case STAR, SLASH, PERCENT -> true;
      default -> false;
    };
  }

  private ParseTree parseExponentiationExpression() {
    SourcePosition start = getTreeStartLocation();
    ParseTree left = parseUnaryExpression();
    if (peek(TokenType.STAR_STAR)) {
      // ExponentiationExpression does not allow a UnaryExpression before '**'.
      // Parentheses are required to disambiguate:
      //   (-x)**y is valid
      //   -(x**y) is valid
      //   -x**y is a syntax error
      if (left.type == ParseTreeType.UNARY_EXPRESSION) {
        reportError(
            "Unary operator '%s' requires parentheses before '**'",
            left.asUnaryExpression().operator);
      }
      Token operator = nextToken();
      ParseTree right = parseExponentiationExpression();
      return new BinaryOperatorTree(getTreeLocation(start), left, operator, right);
    } else {
      return left;
    }
  }

  // 11.4 Unary Operator
  private ParseTree parseUnaryExpression() {
    SourcePosition start = getTreeStartLocation();
    if (peekUnaryOperator()) {
      Token operator = nextToken();
      ParseTree operand = parseUnaryExpression();
      return new UnaryExpressionTree(getTreeLocation(start), operator, operand);
    } else if (peekAwaitExpression()) {
      return parseAwaitExpression();
    } else {
      return parseUpdateExpression();
    }
  }

  private boolean peekUnaryOperator() {
    return switch (peekType()) {
      case DELETE, VOID, TYPEOF, PLUS, MINUS, TILDE, BANG -> true;
      default -> false;
    };
  }

  private static final String AWAIT = "await";

  private boolean peekAwaitExpression() {
    return peekPredefinedString(AWAIT);
  }

  private ParseTree parseAwaitExpression() {
    SourcePosition start = getTreeStartLocation();
    eatPredefinedString(AWAIT);
    ParseTree expression = parseUnaryExpression();
    return new AwaitExpressionTree(getTreeLocation(start), expression);
  }

  private ParseTree parseUpdateExpression() {
    SourcePosition start = getTreeStartLocation();
    if (peekUpdateOperator()) {
      Token operator = nextToken();
      ParseTree operand = parseUnaryExpression();
      return UpdateExpressionTree.prefix(getTreeLocation(start), operator, operand);
    } else {
      ParseTree lhs = parseLeftHandSideExpression();
      if (peekUpdateOperator() && !peekImplicitSemiColon()) {
        // newline not allowed before an update operator.
        Token operator = nextToken();
        return UpdateExpressionTree.postfix(getTreeLocation(start), operator, lhs);
      } else {
        return lhs;
      }
    }
  }

  private boolean peekUpdateOperator() {
    return switch (peekType()) {
      case PLUS_PLUS, MINUS_MINUS -> true;
      default -> false;
    };
  }

  private boolean peekImportCall() {
    return peek(TokenType.IMPORT) && peek(1, TokenType.OPEN_PAREN);
  }

  private boolean peekImportDot() {
    return peek(TokenType.IMPORT) && peek(1, TokenType.PERIOD);
  }

  /** Parse LeftHandSideExpression. */
  @SuppressWarnings("incomplete-switch")
  private ParseTree parseLeftHandSideExpression() {
    SourcePosition start = getTreeStartLocation();
    // We have these possible productions.
    // LeftHandSideExpression -> NewExpression
    //                        -> CallExpression
    //                        -> MemberExpression
    //                        -> OptionalExpression
    //
    // NewExpression -> new NewExpression
    //               -> MemberExpression
    //
    // CallExpression -> MemberExpression Arguments
    //                -> CallExpression ... see below
    //
    // OptionalExpression -> MemberExpression OptionalChain
    //                    -> CallExpression OptionalChain
    //                    -> OptionalExpression OptionalChain
    //
    // We try parsing a NewExpression, here, because that will include parsing MemberExpression.
    // If what we really have is a CallExpression or OptionalExpression, then the MemberExpression
    // we get back from parseNewExpression will be the first part of it, and we'll build the
    // rest later.
    ParseTree operand = parseNewExpression();

    // this test is equivalent to is member expression
    if (!(operand instanceof NewExpressionTree newExpressionTree)
        || newExpressionTree.arguments != null) {
      // We have a MemberExpression, but it may actually be just the first part of a CallExpression
      // Attempt to gather the rest of the CallExpression, if so.
      while (peekCallSuffix()) {
        switch (peekType()) {
          case OPEN_PAREN:
            ArgumentListTree arguments = parseArguments();
            operand = new CallExpressionTree(getTreeLocation(start), operand, arguments);
            break;
          case OPEN_SQUARE:
            eat(TokenType.OPEN_SQUARE);
            ParseTree member = parseExpression();
            eat(TokenType.CLOSE_SQUARE);
            operand = new MemberLookupExpressionTree(getTreeLocation(start), operand, member);
            break;
          case PERIOD:
            eat(TokenType.PERIOD);
            IdentifierToken id = eatIdOrKeywordAsId();
            operand = new MemberExpressionTree(getTreeLocation(start), operand, id);
            break;
          case NO_SUBSTITUTION_TEMPLATE:
          case TEMPLATE_HEAD:
            operand = parseTemplateLiteral(operand);
            break;
          default:
            throw new AssertionError("unexpected case: " + peekType());
        }
      }
      operand = maybeParseOptionalExpression(operand);
    }
    return operand;
  }

  private boolean peekCallSuffix() {
    return peek(TokenType.OPEN_PAREN)
        || peek(TokenType.OPEN_SQUARE)
        || peek(TokenType.PERIOD)
        || peek(TokenType.NO_SUBSTITUTION_TEMPLATE)
        || peek(TokenType.TEMPLATE_HEAD);
  }

  /**
   * Tries to parse the expression as an optional expression.
   *
   * <p>`operand?.identifier` or `operand?.[expression]` or `operand?.(arg1, arg2)`
   *
   * <p>returns parse tree after trying to parse it as an optional expression
   */
  private ParseTree maybeParseOptionalExpression(ParseTree operand) {
    // The optional chain's source info should cover the lhs operand also
    SourcePosition start = operand.location.start;

    while (peek(TokenType.QUESTION_DOT)) {
      eat(TokenType.QUESTION_DOT);
      switch (peekType()) {
        case OPEN_PAREN:
          ArgumentListTree arguments = parseArguments();
          operand =
              new OptChainCallExpressionTree(
                  getTreeLocation(start),
                  operand,
                  arguments,
                  /* isStartOfOptionalChain= */ true,
                  arguments.hasTrailingComma);
          break;
        case OPEN_SQUARE:
          eat(TokenType.OPEN_SQUARE);
          ParseTree member = parseExpression();
          eat(TokenType.CLOSE_SQUARE);
          operand =
              new OptionalMemberLookupExpressionTree(
                  getTreeLocation(start), operand, member, /* isStartOfOptionalChain= */ true);
          break;
        case NO_SUBSTITUTION_TEMPLATE:
        case TEMPLATE_HEAD:
          reportError("template literal cannot be used within optional chaining");
          break;
        default:
          if (peekIdOrKeyword()) {
            IdentifierToken id = eatIdOrKeywordAsId();
            operand =
                new OptionalMemberExpressionTree(
                    getTreeLocation(start), operand, id, /* isStartOfOptionalChain= */ true);
          } else {
            reportError("syntax error: %s not allowed in optional chain", peekType());
          }
      }
      operand = parseRemainingOptionalChainSegment(operand);
    }
    return operand;
  }

  /**
   * Parses the remaining components of an optional chain till the current chain's end, or a new
   * chain's start.
   *
   * <p>`optionalExpression.identifier`, `optionalExpression[expression]`, `optionalExpression(arg1,
   * arg2)`, or `optionalExpression?.optionalExpression`
   *
   * <p>returns parse tree after trying to parse it as an optional chain
   */
  private ParseTree parseRemainingOptionalChainSegment(ParseTree optionalExpression) {
    // The optional chain's source info should cover the lhs operand also
    SourcePosition start = optionalExpression.location.start;
    while (peekOptionalChainSuffix()) {
      if (peekType() == TokenType.NO_SUBSTITUTION_TEMPLATE
          || peekType() == TokenType.TEMPLATE_HEAD) {
        reportError("template literal cannot be used within optional chaining");
        break;
      }
      switch (peekType()) {
        case PERIOD:
          eat(TokenType.PERIOD);
          IdentifierToken id = eatIdOrKeywordAsId();
          optionalExpression =
              new OptionalMemberExpressionTree(
                  getTreeLocation(start),
                  optionalExpression,
                  id,
                  /* isStartOfOptionalChain= */ false);
          break;
        case OPEN_PAREN:
          ArgumentListTree arguments = parseArguments();
          optionalExpression =
              new OptChainCallExpressionTree(
                  getTreeLocation(start),
                  optionalExpression,
                  arguments,
                  /* isStartOfOptionalChain= */ false,
                  arguments.hasTrailingComma);
          break;
        case OPEN_SQUARE:
          eat(TokenType.OPEN_SQUARE);
          ParseTree member = parseExpression();
          eat(TokenType.CLOSE_SQUARE);
          optionalExpression =
              new OptionalMemberLookupExpressionTree(
                  getTreeLocation(start),
                  optionalExpression,
                  member,
                  /* isStartOfOptionalChain= */ false);
          break;
        default:
          throw new AssertionError("unexpected case: " + peekType());
      }
    }
    return optionalExpression;
  }

  /** Tokens that indicate continuation of an optional chain. */
  private boolean peekOptionalChainSuffix() {
    return peek(TokenType.OPEN_PAREN) // a?.b( ...
        || peek(TokenType.OPEN_SQUARE) // a?.b[ ...
        || peek(TokenType.PERIOD) // a?.b. ...
        // TEMPLATE_HEAD and NO_SUBSTITUTION_TEMPLATE are actually not allowed within optional
        // chaining and leads to an early error as dictated by the spec.
        // https://tc39.es/proposal-optional-chaining/#sec-left-hand-side-expressions-static-semantics-early-errors
        || peek(TokenType.NO_SUBSTITUTION_TEMPLATE) // a?.b`text`
        || peek(TokenType.TEMPLATE_HEAD); // a?.b`text ${substitution} text`
  }

  private static final String ASYNC = "async";

  // 11.2 Member Expression without the new production
  private ParseTree parseMemberExpressionNoNew() {
    SourcePosition start = getTreeStartLocation();
    ParseTree operand;
    if (peekImportDot()) {
      operand = parseImportDotMeta();
    } else if (peekAsyncFunctionStart()) {
      operand = parseAsyncFunctionExpression();
    } else if (peekFunction()) {
      operand = parseFunctionExpression();
    } else {
      operand = parsePrimaryExpression();
    }
    while (peekMemberExpressionSuffix()) {
      switch (peekType()) {
        case OPEN_SQUARE:
          eat(TokenType.OPEN_SQUARE);
          ParseTree member = parseExpression();
          eat(TokenType.CLOSE_SQUARE);
          operand = new MemberLookupExpressionTree(getTreeLocation(start), operand, member);
          break;
        case PERIOD:
          eat(TokenType.PERIOD);
          IdentifierToken id = eatIdOrKeywordAsId();
          operand = new MemberExpressionTree(getTreeLocation(start), operand, id);
          break;
        case NO_SUBSTITUTION_TEMPLATE:
        case TEMPLATE_HEAD:
          operand = parseTemplateLiteral(operand);
          break;
        default:
          throw new RuntimeException("unreachable");
      }
    }
    return operand;
  }

  private boolean peekMemberExpressionSuffix() {
    return peek(TokenType.OPEN_SQUARE)
        || peek(TokenType.PERIOD)
        || peek(TokenType.NO_SUBSTITUTION_TEMPLATE)
        || peek(TokenType.TEMPLATE_HEAD);
  }

  private ParseTree parseNewExpression() {
    if (!peek(TokenType.NEW)) {
      return parseMemberExpressionNoNew();
    } else if (peek(1, TokenType.PERIOD)) {
      return parseNewDotSomething();
    } else {
      SourcePosition start = getTreeStartLocation();
      eat(TokenType.NEW);
      if (peek(TokenType.QUESTION_DOT)) { // new?.target not allowed
        reportError("Optional chaining is forbidden in `new?.target` contexts.");
      }
      ParseTree operand = parseNewExpression();
      if (peek(TokenType.QUESTION_DOT)) { // new a?.() not allowed
        reportError("Optional chaining is forbidden in construction contexts.");
      }
      ArgumentListTree arguments = null;
      if (peek(TokenType.OPEN_PAREN)) {
        arguments = parseArguments();
      }
      return new NewExpressionTree(
          getTreeLocation(start),
          operand,
          arguments,
          arguments != null && arguments.hasTrailingComma);
    }
  }

  private ParseTree parseNewDotSomething() {
    // currently only "target" is valid after "new."
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.NEW);
    eat(TokenType.PERIOD);
    eatPredefinedString("target");
    return new NewTargetExpressionTree(getTreeLocation(start));
  }

  private ParseTree parseImportDotMeta() {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.IMPORT);
    eat(TokenType.PERIOD);
    eatPredefinedString("meta");
    return new ImportMetaExpressionTree(getTreeLocation(start));
  }

  private ArgumentListTree parseArguments() {
    // ArgumentList :
    //   AssignmentOrSpreadExpression
    //   ArgumentList , AssignmentOrSpreadExpression
    //
    // AssignmentOrSpreadExpression :
    //   ... AssignmentExpression
    //   AssignmentExpression

    SourcePosition start = getTreeStartLocation();
    ImmutableList.Builder<ParseTree> arguments = ImmutableList.builder();
    boolean trailingComma = false;
    ImmutableList.Builder<SourcePosition> commaPositions = ImmutableList.builder();

    eat(TokenType.OPEN_PAREN);
    while (peekAssignmentOrSpread()) {
      arguments.add(parseAssignmentOrSpread());

      if (!peek(TokenType.CLOSE_PAREN)) {
        Token comma = eat(TokenType.COMMA);
        if (comma != null) {
          commaPositions.add(comma.getStart());
        }
        if (peek(TokenType.CLOSE_PAREN)) {
          if (!config.atLeast8) {
            reportError(comma, "Invalid trailing comma in arguments list");
          }
          trailingComma = true;
        }
      }
    }
    eat(TokenType.CLOSE_PAREN);
    return new ArgumentListTree(
        getTreeLocation(start), arguments.build(), trailingComma, commaPositions.build());
  }

  /**
   * Whether we have a spread expression or an assignment next.
   *
   * <p>This does not peek the operand for the spread expression. This means that {@link
   * #parseAssignmentOrSpread} might still fail when this returns true.
   */
  private boolean peekAssignmentOrSpread() {
    return peek(TokenType.ELLIPSIS) || peekAssignmentExpression();
  }

  private ParseTree parseAssignmentOrSpread() {
    if (peek(TokenType.ELLIPSIS)) {
      return parseIterSpread();
    }
    return parseAssignmentExpression();
  }

  // Destructuring (aka pattern matching); see
  // http://wiki.ecmascript.org/doku.php?id=harmony:destructuring

  // Kinds of destructuring patterns
  private enum PatternKind {
    // A var, let, const; catch head; or formal parameter list--only
    // identifiers are allowed as lvalues
    INITIALIZER,
    // An assignment or for-in initializer--any lvalue is allowed
    ANY,
  }

  private boolean peekPatternStart() {
    return peek(TokenType.OPEN_SQUARE) || peek(TokenType.OPEN_CURLY);
  }

  private ParseTree parsePattern(PatternKind kind) {
    switch (peekType()) {
      case OPEN_SQUARE:
        return parseArrayPattern(kind);
      case OPEN_CURLY:
      default:
        return parseObjectPattern(kind);
    }
  }

  private boolean peekArrayPatternElement() {
    return peekExpression();
  }

  private ParseTree parseIterRest(PatternKind patternKind) {
    SourcePosition start = getTreeStartLocation();
    eat(TokenType.ELLIPSIS);
    ParseTree patternAssignmentTarget = parseRestAssignmentTarget(patternKind);
    return new IterRestTree(getTreeLocation(start), patternAssignmentTarget);
  }

  private ParseTree parseRestAssignmentTarget(PatternKind patternKind) {
    ParseTree patternAssignmentTarget = parsePatternAssignmentTargetNoDefault(patternKind);
    if (peek(TokenType.EQUAL)) {
      reportError("A default value cannot be specified after '...'");
    }
    return patternAssignmentTarget;
  }

  // Pattern ::= ... | "[" Element? ("," Element?)* "]"
  private ParseTree parseArrayPattern(PatternKind kind) {
    SourcePosition start = getTreeStartLocation();
    ImmutableList.Builder<ParseTree> elements = ImmutableList.builder();
    eat(TokenType.OPEN_SQUARE);
    while (peek(TokenType.COMMA) || peekArrayPatternElement()) {
      if (peek(TokenType.COMMA)) {
        SourcePosition nullStart = getTreeStartLocation();
        eat(TokenType.COMMA);
        elements.add(new NullTree(getTreeLocation(nullStart)));
      } else {
        elements.add(parsePatternAssignmentTarget(kind));

        if (peek(TokenType.COMMA)) {
          // Consume the comma separator
          eat(TokenType.COMMA);
        } else {
          // Otherwise we must be done
          break;
        }
      }
    }
    if (peek(TokenType.ELLIPSIS)) {
      recordFeatureUsed(Feature.ARRAY_PATTERN_REST);
      elements.add(parseIterRest(kind));
    }
    if (eat(TokenType.CLOSE_SQUARE) == null) {
      // If we get no closing bracket then return invalid tree to avoid compiler tripping
      // downstream. It's needed only for IDE mode where compiler continues processing even if
      // source has syntactic errors.
      return new MissingPrimaryExpressionTree(getTreeLocation(getTreeStartLocation()));
    }
    return new ArrayPatternTree(getTreeLocation(start), elements.build());
  }

  // Pattern ::= "{" (Field ("," Field)* ","?)? "}" | ...
  private ParseTree parseObjectPattern(PatternKind kind) {
    SourcePosition start = getTreeStartLocation();
    ImmutableList.Builder<ParseTree> fields = ImmutableList.builder();
    eat(TokenType.OPEN_CURLY);
    while (peekObjectPatternField()) {
      fields.add(parseObjectPatternField(kind));

      if (peek(TokenType.COMMA)) {
        // Consume the comma separator
        eat(TokenType.COMMA);
      } else {
        // Otherwise we must be done
        break;
      }
    }
    if (peek(TokenType.ELLIPSIS)) {
      recordFeatureUsed(Feature.OBJECT_PATTERN_REST);
      SourcePosition restStart = getTreeStartLocation();
      eat(TokenType.ELLIPSIS);
      ParseTree patternAssignmentTarget = parseRestAssignmentTarget(kind);
      fields.add(new ObjectRestTree(getTreeLocation(restStart), patternAssignmentTarget));
    }
    eat(TokenType.CLOSE_CURLY);
    return new ObjectPatternTree(getTreeLocation(start), fields.build());
  }

  private boolean peekObjectPatternField() {
    return peekPropertyNameOrComputedProp(0);
  }

  private ParseTree parseObjectPatternField(PatternKind kind) {
    SourcePosition start = getTreeStartLocation();
    if (peekType() == TokenType.OPEN_SQUARE) {
      ParseTree key = parseComputedPropertyName();
      eat(TokenType.COLON);
      ParseTree value = parsePatternAssignmentTarget(kind);
      return new ComputedPropertyDefinitionTree(getTreeLocation(start), key, value);
    }

    Token name;
    if (peekIdOrKeyword()) {
      IdentifierToken idToken = eatIdOrKeywordAsId();
      if (!peek(TokenType.COLON)) {
        if (idToken.isKeyword()) {
          reportError("cannot use keyword '%s' here.", idToken);
        }
        if (peek(TokenType.EQUAL)) {
          IdentifierExpressionTree idTree =
              new IdentifierExpressionTree(getTreeLocation(start), idToken);
          eat(TokenType.EQUAL);
          ParseTree defaultValue = parseAssignmentExpression();
          return new DefaultParameterTree(getTreeLocation(start), idTree, defaultValue);
        }
        return new PropertyNameAssignmentTree(getTreeLocation(start), idToken, null);
      }
      name = idToken;
    } else {
      name = parseLiteralExpression().literalToken;
    }

    eat(TokenType.COLON);
    ParseTree value = parsePatternAssignmentTarget(kind);
    return new PropertyNameAssignmentTree(getTreeLocation(start), name, value);
  }

  /**
   * A PatternAssignmentTarget is the location where the assigned value gets stored, including an
   * optional default value.
   *
   * <dl>
   *   <dt>Spec AssignmentElement === PatternAssignmentTarget(PatternKind.ANY)
   *   <dd>Valid in an assignment that is not a formal parameter list or variable declaration.
   *       Sub-patterns and arbitrary left hand side expressions are allowed.
   *   <dt>Spec BindingElement === PatternAssignmentElement(PatternKind.INITIALIZER)
   *   <dd>Valid in a formal parameter list or variable declaration statement. Only sub-patterns and
   *       identifiers are allowed.
   * </dl>
   *
   * Examples:
   *
   * <pre>
   *   <code>
   *     [a, {foo: b = 'default'}] = someArray;          // valid
   *     [x.a, {foo: x.b = 'default'}] = someArray;      // valid
   *
   *     let [a, {foo: b = 'default'}] = someArray;      // valid
   *     let [x.a, {foo: x.b = 'default'}] = someArray;  // invalid
   *
   *     function f([a, {foo: b = 'default'}]) {...}     // valid
   *     function f([x.a, {foo: x.b = 'default'}]) {...} // invalid
   *   </code>
   * </pre>
   */
  private ParseTree parsePatternAssignmentTarget(PatternKind patternKind) {
    SourcePosition start = getTreeStartLocation();
    ParseTree assignmentTarget;
    assignmentTarget = parsePatternAssignmentTargetNoDefault(patternKind);

    if (peek(TokenType.EQUAL)) {
      eat(TokenType.EQUAL);
      ParseTree defaultValue = parseAssignmentExpression();
      assignmentTarget =
          new DefaultParameterTree(getTreeLocation(start), assignmentTarget, defaultValue);
    }
    return assignmentTarget;
  }

  private ParseTree parsePatternAssignmentTargetNoDefault(PatternKind kind) {
    ParseTree assignmentTarget;
    if (peekPatternStart()) {
      assignmentTarget = parsePattern(kind);
    } else {
      assignmentTarget = parseLeftHandSideExpression();
      if (!assignmentTarget.isValidAssignmentTarget()) {
        reportError("invalid assignment target");
      }
      if (kind == PatternKind.INITIALIZER
          && assignmentTarget.type != ParseTreeType.IDENTIFIER_EXPRESSION) {
        // We're in the context of a formal parameter list or a variable declaration statement
        reportError("Only an identifier or destructuring pattern is allowed here.");
      }
    }
    return assignmentTarget;
  }

  /** Consume a (possibly implicit) semi-colon. Reports an error if a semi-colon is not present. */
  private void eatPossiblyImplicitSemiColon() {
    if (peek(TokenType.SEMI_COLON)) {
      eat(TokenType.SEMI_COLON);
      return;
    }
    if (peekImplicitSemiColon()) {
      return;
    }

    reportError("Semi-colon expected");
  }

  /** Returns true if an implicit or explicit semi colon is at the current location. */
  private boolean peekImplicitSemiColon() {
    return peekImplicitSemiColon(0);
  }

  private boolean peekImplicitSemiColon(int index) {
    boolean lineAdvanced;
    if (index == 0) {
      lineAdvanced = getNextLine() > getLastLine();
    } else {
      lineAdvanced = peekToken(index).location.start.line > peekToken(index - 1).location.end.line;
    }
    return lineAdvanced
        || peek(index, TokenType.SEMI_COLON)
        || peek(index, TokenType.CLOSE_CURLY)
        || peek(index, TokenType.END_OF_FILE);
  }

  /** Returns the line number of the most recently consumed token. */
  private int getLastLine() {
    return lastSourcePosition.line;
  }

  /** Returns the line number of the next token. */
  private int getNextLine() {
    return peekToken().location.start.line;
  }

  /**
   * Consumes the next token if it is of the expected type. Otherwise returns null. Never reports
   * errors.
   *
   * @return The consumed token, or null if the next token is not of the expected type.
   */
  private @Nullable Token eatOpt(TokenType expectedTokenType) {
    if (peek(expectedTokenType)) {
      return eat(expectedTokenType);
    }
    return null;
  }

  private boolean inStrictContext() {
    // TODO(johnlenz): track entering strict scripts/modules/functions.
    return config.isStrictMode;
  }

  private boolean peekId() {
    return peekId(0);
  }

  /**
   * @return whether the next token is an identifier.
   */
  private boolean peekId(int index) {
    TokenType type = peekType(index);
    // There is one special case to handle here: outside of strict-mode code, strict-mode keywords
    // can be used as identifiers
    return TokenType.IDENTIFIER == type || (!inStrictContext() && Keywords.isStrictKeyword(type));
  }

  private boolean peekIdOrKeyword() {
    return peekIdOrKeyword(0);
  }

  private boolean peekIdOrKeyword(int index) {
    TokenType type = peekType(index);
    return TokenType.IDENTIFIER == type || Keywords.isKeyword(type);
  }

  /** Shorthand for eatOpt(TokenType.IDENTIFIER) */
  private @Nullable IdentifierToken eatIdOpt() {
    return (peekId()) ? eatIdOrKeywordAsId() : null;
  }

  /**
   * Consumes an identifier token that is not a reserved word.
   *
   * @see "http://www.ecma-international.org/ecma-262/5.1/#sec-7.6"
   */
  private @Nullable IdentifierToken eatId() {
    if (peekId()) {
      return eatIdOrKeywordAsId();
    } else {
      reportExpectedError(peekToken(), TokenType.IDENTIFIER);
      if (peekIdOrKeyword()) {
        return eatIdOrKeywordAsId();
      } else {
        return null;
      }
    }
  }

  private Token eatObjectLiteralPropertyName() {
    Token token = peekToken();
    switch (token.type) {
      case STRING:
      case NUMBER:
      case BIGINT:
        return nextToken();
      case IDENTIFIER:
      default:
        return eatIdOrKeywordAsId();
    }
  }

  /**
   * Consumes an identifier token that may be a reserved word, i.e. an IdentifierName, not
   * necessarily an Identifier.
   *
   * @see "http://www.ecma-international.org/ecma-262/5.1/#sec-7.6"
   */
  private @Nullable IdentifierToken eatIdOrKeywordAsId() {
    Token token = nextToken();
    if (token.type == TokenType.IDENTIFIER) {
      return (IdentifierToken) token;
    } else if (Keywords.isKeyword(token.type)) {
      return new IdentifierToken(token.location, Keywords.get(token.type).toString());
    } else {
      reportExpectedError(token, TokenType.IDENTIFIER);
    }
    return null;
  }

  /**
   * Consumes the next token. If the consumed token is not of the expected type then report an error
   * and return null. Otherwise return the consumed token.
   *
   * @return The consumed token, or null if the next token is not of the expected type.
   */
  private @Nullable Token eat(TokenType expectedTokenType) {
    Token token = nextToken();
    if (token.type != expectedTokenType) {
      reportExpectedError(token, expectedTokenType);
      return null;
    }
    return token;
  }

  /**
   * Report a 'X' expected error message.
   *
   * @param token The location to report the message at.
   * @param expected The thing that was expected.
   */
  private void reportExpectedError(@Nullable Token token, Object expected) {
    reportError(token, "'%s' expected", expected);
  }

  /** Returns a SourcePosition for the start of a parse tree that starts at the current location. */
  private SourcePosition getTreeStartLocation() {
    return peekToken().location.start;
  }

  /** Returns a SourcePosition for the end of a parse tree that ends at the current location. */
  private SourcePosition getTreeEndLocation() {
    return lastSourcePosition;
  }

  /**
   * Returns a SourceRange for a parse tree that starts at {start} and ends at the current location.
   */
  private SourceRange getTreeLocation(SourcePosition start) {
    return new SourceRange(start, getTreeEndLocation());
  }

  /**
   * Consumes the next token and returns it. Will return a never ending stream of
   * TokenType.END_OF_FILE at the end of the file so callers don't have to check for EOF explicitly.
   *
   * <p>Tokenizing is contextual. nextToken() will never return a regular expression literal.
   */
  private Token nextToken() {
    Token token = scanner.nextToken();
    lastSourcePosition = token.location.end;
    return token;
  }

  /** Consumes a regular expression literal token and returns it. */
  private LiteralToken nextRegularExpressionLiteralToken() {
    LiteralToken token = scanner.nextRegularExpressionLiteralToken();
    lastSourcePosition = token.location.end;
    return token;
  }

  /** Consumes a template literal token and returns it. */
  private TemplateLiteralToken nextTemplateLiteralToken() {
    TemplateLiteralToken token = scanner.nextTemplateLiteralToken();
    lastSourcePosition = token.location.end;
    return token;
  }

  /** Returns true if the next token is of the expected type. Does not consume the token. */
  private boolean peek(TokenType expectedType) {
    return peek(0, expectedType);
  }

  /**
   * Returns true if the index-th next token is of the expected type. Does not consume any tokens.
   */
  private boolean peek(int index, TokenType expectedType) {
    return peekType(index) == expectedType;
  }

  /** Returns the TokenType of the next token. Does not consume any tokens. */
  private TokenType peekType() {
    return peekType(0);
  }

  /** Returns the TokenType of the index-th next token. Does not consume any tokens. */
  private TokenType peekType(int index) {
    return peekToken(index).type;
  }

  /** Returns the next token. Does not consume any tokens. */
  private Token peekToken() {
    return peekToken(0);
  }

  /** Returns the index-th next token. Does not consume any tokens. */
  private Token peekToken(int index) {
    return scanner.peekToken(index);
  }

  /**
   * Reports an error message at a given token.
   *
   * @param token The location to report the message at.
   * @param message The message to report in String.format style.
   * @param arguments The arguments to fill in the message format.
   */
  @FormatMethod
  private void reportError(Token token, @FormatString String message, Object... arguments) {
    if (token == null) {
      reportError(message, arguments);
    } else {
      errorReporter.reportError(token.getStart(), message, arguments);
    }
  }

  /**
   * Reports an error message at a given parse tree's location.
   *
   * @param parseTree The location to report the message at.
   * @param message The message to report in String.format style.
   * @param arguments The arguments to fill in the message format.
   */
  @FormatMethod
  private void reportError(ParseTree parseTree, @FormatString String message, Object... arguments) {
    if (parseTree == null) {
      reportError(message, arguments);
    } else {
      errorReporter.reportError(parseTree.location.start, message, arguments);
    }
  }

  /**
   * Reports an error at the current location.
   *
   * @param message The message to report in String.format style.
   * @param arguments The arguments to fill in the message format.
   */
  @FormatMethod
  private void reportError(@FormatString String message, Object... arguments) {
    errorReporter.reportError(scanner.getPosition(), message, arguments);
  }

  /**
   * Reports an error at the specified location.
   *
   * @param position The position of the error.
   * @param message The message to report in String.format style.
   * @param arguments The arguments to fill in the message format.
   */
  @FormatMethod
  private void reportError(
      SourcePosition position, @FormatString String message, Object... arguments) {
    errorReporter.reportError(position, message, arguments);
  }

  private void reportTemplateErrorIfPresent(TemplateLiteralToken templateToken) {
    if (templateToken.errorMessage == null) {
      return;
    }
    switch (templateToken.errorLevel) {
      case WARNING:
        errorReporter.reportWarning(templateToken.errorPosition, "%s", templateToken.errorMessage);
        return;
      case ERROR:
        reportError(templateToken.errorPosition, "%s", templateToken.errorMessage);
        return;
    }
    throw new AssertionError();
  }

  @CanIgnoreReturnValue
  private Parser recordFeatureUsed(Feature feature) {
    features = features.with(feature);
    return this;
  }
}
