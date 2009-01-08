package werkzeugkasten.twowaysql.editor.contentassist;

import static werkzeugkasten.twowaysql.grammar.TwoWaySqlLexer.BEGIN;
import static werkzeugkasten.twowaysql.grammar.TwoWaySqlLexer.ELSE;
import static werkzeugkasten.twowaysql.grammar.TwoWaySqlLexer.ELSEIF;
import static werkzeugkasten.twowaysql.grammar.TwoWaySqlLexer.IDENT;
import static werkzeugkasten.twowaysql.grammar.TwoWaySqlLexer.IF;
import static werkzeugkasten.twowaysql.grammar.TwoWaySqlLexer.LT;
import static werkzeugkasten.twowaysql.grammar.TwoWaySqlLexer.WHITE_SPACES;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import werkzeugkasten.twowaysql.Constants;
import werkzeugkasten.twowaysql.grammar.TwoWaySqlLexer;

public class CommentContentAssistProcessor implements IContentAssistProcessor {

	protected String errorMessage;

	protected static final String[] KEYWORDS = { "BEGIN", "IF", "ELSEIF",
			"ELSE" };

	protected static final BitSet WHITESPACE_bits = BitSet.of(LT, WHITE_SPACES);
	protected static final BitSet KEYWORD_bits = BitSet.of(BEGIN, IF, ELSEIF,
			ELSE);
	protected static final BitSet KEYWORDPART_bits = BitSet.of(IDENT, ELSE);

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		try {
			IDocument doc = viewer.getDocument();
			ITypedRegion partition = null;
			if (doc instanceof IDocumentExtension3) {
				IDocumentExtension3 de3 = (IDocumentExtension3) doc;
				partition = de3.getPartition(
						Constants.PARTITION_TYPE_TWOWAYSQL, offset, true);
			} else {
				partition = doc.getPartition(offset);
			}

			int cursor = offset - partition.getOffset() - 1;
			String string = doc.get(partition.getOffset(), partition
					.getLength());
			CommonTokenStream tokens = new CommonTokenStream(
					new TwoWaySqlLexer(new ANTLRStringStream(string)) {
						@Override
						public Token emit() {
							state.channel = org.antlr.runtime.Token.DEFAULT_CHANNEL;
							return super.emit();
						}
					});
			List<CommonToken> before = new ArrayList<CommonToken>();
			CommonToken ct = (CommonToken) tokens.LT(1);
			while (true) {
				if (WHITESPACE_bits.member(ct.getType()) == false) {
					before.add(ct);
				}
				if (ct.getStopIndex() < cursor) {
					tokens.consume();
					ct = (CommonToken) tokens.LT(1);
				} else {
					break;
				}
			}
			if (before.size() < 2) {
				// キーワード全部(コメント開始位置もしくは、その内側にカーソルがある)
				CommonToken first = before.get(0);
				for (int i = 0; i < KEYWORDS.length; i++) {
					CompletionProposal cp = new CompletionProposal(KEYWORDS[i],
							partition.getOffset() + first.getStopIndex() + 1,
							0, KEYWORDS[i].length());
					proposals.add(cp);
				}
			} else {
				CommonToken second = before.get(1);
				// キーワード部分(開始位置にいくつか文字列がある。キーワードリストからフィルタして渡す)
				if (KEYWORDPART_bits.member(second.getType())) {
					String txt = second.getText().toUpperCase();
					for (int i = 0; i < KEYWORDS.length; i++) {
						String s = KEYWORDS[i];
						if (s.startsWith(txt) && txt.endsWith(s) == false) {
							CompletionProposal cp = new CompletionProposal(s,
									partition.getOffset()
											+ second.getStartIndex(), txt
											.length(), s.length());
							proposals.add(cp);
						}
					}
				}
				// キーワードが既に完成している。(式言語の入力補完)
				if (KEYWORD_bits.member(second.getType())) {
					// TODO 式言語の入力補完
				}
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadPartitioningException e) {
			return null;
		} catch (BadLocationException e) {
			return null;
		}
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return this.errorMessage;
	}

}