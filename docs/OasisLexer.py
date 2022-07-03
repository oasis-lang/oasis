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
            (r'\b(if)\b', Keyword),
            (r'\b(else)\b', Keyword),
            (r'\b(while)\b', Keyword),
            (r'\b(for)\b', Keyword),
            (r'\b(break)\b', Keyword),
            (r'\b(continue)\b', Keyword),
            (r'\b(return)\b', Keyword),
            (r'\b(true|false)\b', Keyword.Type),
            (r'\b(nil)\b', Keyword.Constant),
            (r'\b(let|const|rel)\b', Keyword.Declaration),
            (r'\b(proto)\b', Keyword),
            (r'\b(fn)\b', Keyword),
            (r'\b(end)\b', Keyword),
            (r'\b(and)\b', Operator.Word),
            (r'\b(or)\b', Operator.Word),
            (r'\b(not)\b', Operator.Word),
            (r'\b(in)\b', Keyword),
            (r'\b(of)\b', Operator.Word),
            (r'\b(clone)\b', Operator.Word),
            (r'\b(is)\b', Keyword),
            (r'\b(test)\b', Keyword),
            (r'\b(error)\b', Keyword),
            (r'\b(break)\b', Keyword),
            (r'\b(continue)\b', Keyword),
            (r'\b(do)\b', Keyword),
            (r'"[^"]*"', String.Double),
            (r'\'.\'', String.Char),
            (r'(\+|\-|\*|\/|\%|\=|\=\=|\!\=|\,|\.|\>|\>\=|\<|\<\=|\=\>|\||\|\>|\<\||\?|\:)', Operator),
            (r'(\d+\.\d*|\d*\.\d+)([eE][+-]?\d+)?', Number.Float),
            (r'\d+([eE][+-]?\d+)?', Number.Integer),
            (r'\$[a-zA-Z0-9_]+', Name.Variable),
            (r'[a-zA-Z_][a-zA-Z0-9_]*', Name),
            (r'(\(|\)|\[|\]|\{|\})', Punctuation),
            (r'\s+', Text),

        ],
    }

lexers['oasis'] = OasisLexer()
