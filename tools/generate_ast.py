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

        # visitor
        indent += 1
        define_visitor(f, indent, baseclass, classes)

        for cl in classes:
            name = cl['class']
            fields = cl['fields']

            # class
            write_code(f, indent, 'static class %s extends %s {\n' % (name, baseclass))

            # constructor
            indent += 1
            write_code(f, indent, '%s(%s) {\n' % (name, ', '.join(fields)))

            for field in fields:
                var = field.split()[1]
                write_code(f, indent + 1, 'this.%s = %s;\n' % (var, var))

            # end constructor
            write_code(f, indent, '}\n\n')

            # visitor pattern
            write_code(f, indent, '@Override\n')
            write_code(f, indent, '<R> R accept(Visitor<R> visitor) {\n')
            write_code(f, indent + 1, 'return visitor.visit%s%s(this);\n' % (name, baseclass)) 
            write_code(f, indent, '}\n\n')

            # member vars
            for field in fields:
                write_code(f, indent, 'final %s;\n' % field )

            # end class
            indent -= 1
            write_code(f, indent, '}\n\n')

        # base class accept method
        write_code(f, indent, "abstract <R> R accept(Visitor<R> visitor);\n")

        # end base class
        indent -= 1
        write_code(f, indent, '}\n')

    print('Created %s' % file)


def define_visitor(f, indent, baseclass, classes):
    write_code(f, indent, 'interface Visitor<R> {\n')

    for c in classes:
        name = c['class']
        write_code(f, indent + 1, 'R visit%s%s(%s expr);\n' % (name, baseclass, name))

    write_code(f, indent, '}\n\n')


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
                {'class': 'Conditional', 'fields': ['Expr condition', 'Expr thenBranch', 'Expr elseBranch']},
            ]
    )

    define_ast(output_dir, 'Stmt',
            [
                {'class': 'Expression', 'fields': ['Expr expression']},
                {'class': 'Print', 'fields': ['Expr expression']},
            ]
    )
