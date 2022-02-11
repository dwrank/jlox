#!/usr/bin/env python

import sys
import os


def define_ast(output_dir, baseclass, classes):
    file = os.path.join(output_dir, baseclass) + '.java'

    with open(file, 'w') as f:
        indent = 0

        # header
        write_code(f, indent, 'package com.drank.lox;\n\n')
        write_code(f, indent, 'import java.util.List;\n\n')

        # base class
        write_code(f, indent, 'abstract class %s {\n' % baseclass)

        indent += 1
        for cl in classes:
            name = cl['class']
            fields = cl['fields']

            # class
            write_code(f, indent, 'static class %s extends %s {\n' % (name, baseclass))

            # constructor
            indent += 1
            write_code(f, indent, '%s(%s) {\n' % (name, ', '.join(fields)))

            indent += 1
            for field in fields:
                var = field.split()[1]
                write_code(f, indent, 'this.%s = %s;\n' % (var, var))

            # end constructor
            indent -= 1
            write_code(f, indent, '}\n\n')

            # member vars
            for field in fields:
                write_code(f, indent, 'final %s;\n' % field )

            # end class
            indent -= 1
            write_code(f, indent, '}\n\n')

        # end base class
        indent -= 1
        write_code(f, indent, '}\n')

    print('Created %s' % file)


def write_code(f, indent, s):
    indent = ' ' * (indent * 4)
    f.write('%s%s' % (indent, s))


if __name__ == '__main__':
    try:
        output_dir = sys.argv[1]
    except:
        output_dir = 'java/com/drank/lox'

    define_ast(output_dir, 'Expr',
            [
                {'class': 'Binary', 'fields': ['Expr left', 'Token operator', 'Expr right']},
                {'class': 'Grouping', 'fields': ['Expr expression']},
                {'class': 'Literal', 'fields': ['Object value']},
                {'class': 'Unary', 'fields': ['Token operator', 'Expr right']},
            ]
    )
