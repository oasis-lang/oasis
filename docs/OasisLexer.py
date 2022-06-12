from pygments.lexer import RegexLexer
from pygments.token import Text, Comment, Keyword, Name, String, Number, Punctuation, Operator, Generic
from sphinx.highlighting import lexers

class OasisLexer(RegexLexer):
    name = 'Oasis'
    aliases = ['oasis']
    filenames = ['*.oa']

    tokens = {
        'root': [
            (r'//.*?$', Comment.Single),
            (r'#.*?$', Comment.Single),
            (r'(if)', Keyword),
            (r'(else)', Keyword),
            (r'(while)', Keyword),
            (r'(for)', Keyword),
            (r'(break)', Keyword),
            (r'(continue)', Keyword),
            (r'(return)', Keyword),
            (r'(true|false)', Keyword.Constant),
            (r'(nil)', Keyword.Constant),
            (r'(let|immutable)', Keyword.Declaration),
            (r'(proto)', Keyword.Type),
            (r'(fn)', Keyword.Type),
            (r'(end)', Keyword),
            (r'(and)', Operator.Word),
            (r'(or)', Operator.Word),
            (r'(not)', Operator.Word),
            (r'(in)', Keyword),
            (r'(of)', Operator.Word),
            (r'(clone)', Operator.Word),
            (r'(is)', Keyword),
            (r'(test)', Keyword),
            (r'(error)', Keyword),
            (r'(break)', Keyword),
            (r'(continue)', Keyword),
            (r'"[^"]*"', String.Double),
            (r'\'[^\']\'', String.Char),
            (r'(\+|\-|\*|\/|\%|\=)', Operator),
            (r'(\d+\.\d*|\d*\.\d+)([eE][+-]?\d+)?', Number.Float),
            (r'\d+([eE][+-]?\d+)?', Number.Integer),
            (r'\$[a-zA-Z0-9_]+', Name.Variable),
            (r'[a-zA-Z_][a-zA-Z0-9_]*', Name),
            (r'(\(|\)|\[|\]|\{|\})', Punctuation),
            (r'\:', Punctuation),
            (r'\s+', Text),

        ],
    }

lexers['oasis'] = OasisLexer()
