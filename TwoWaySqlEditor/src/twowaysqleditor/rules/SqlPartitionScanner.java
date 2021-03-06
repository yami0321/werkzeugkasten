package twowaysqleditor.rules;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class SqlPartitionScanner extends RuleBasedPartitionScanner {
	public static final String SQL_IF = "__sql_if";
	public static final String SQL_ELSE = "__sql_else";
	public static final String SQL_BEGIN = "__sql_begin";
	public static final String SQL_END = "__sql_end";
	public static final String SQL_BIND = "__sql_bind";
	public static final String SQL_LINE_COMMENT = "__sql_line_comment";
	public static final String SQL_COMMENT = "__sql_comment";

	public static final String[] PARTITIONS = { SQL_IF, SQL_ELSE, SQL_BEGIN,
			SQL_END, SQL_BIND, SQL_LINE_COMMENT, SQL_COMMENT };

	public SqlPartitionScanner() {
		IToken sqlIf = new Token(SQL_IF);
		IToken sqlElse = new Token(SQL_ELSE);
		IToken sqlBegin = new Token(SQL_BEGIN);
		IToken sqlEnd = new Token(SQL_END);
		IToken sqlLineComment = new Token(SQL_LINE_COMMENT);
		IToken sqlComment = new Token(SQL_COMMENT);

		IPredicateRule[] rules = { new SingleLineRule("/*IF", "*/", sqlIf),
				new SingleLineRule("-- ELSE", null, sqlElse),
				new SingleLineRule("/*END", "*/", sqlEnd),
				new SingleLineRule("/*BEGIN", "*/", sqlBegin),
				new SingleLineRule("--", null, sqlLineComment),
				new MultiLineRule("/*", "*/", sqlComment) };

		setPredicateRules(rules);
	}
}
