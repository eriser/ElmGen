#!/usr/bin/python2.6
#
# Copyright 2014 Olivier Gillet.
#
# Author: Olivier Gillet (ol.gillet@gmail.com)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
# -----------------------------------------------------------------------------
#
# Converts an assembly program to an ElmGen java class

"""Converts an assembly program to an ElmGen java class.

Usage: spn2java my_awesome_effect.spn MyAwesomeEffect.java
"""

import os
import re
import sys

def strip_spaces(line):
  return ' '.join([x for x in re.split('[ \t \r\n]+', line) if x])

def output(code, class_name='MyEffect', source=None):
  yield 'import org.andrewkilpatrick.elmGen.ElmProgram;'
  if source:
    for line in source:
      yield '// ' + line.strip()
  yield 'public class %s extends ElmProgram {' % class_name
  yield '  public %s() {' % class_name
  yield '    super("%s");' % class_name
  yield '    setSamplerate(48000);'
  for translated_line in code:
    yield '    ' + translated_line
  yield '  }'
  yield '}'

def chorus_flags(expression):
  return '|'.join(x if '0x' in x or x.isdigit() else 'CHO_%s' % x.upper() for x in expression.split('|'))

def name_offset(x, names):
  if x in names:
    return names[x]
  elif x.endswith('#'):
    return x.strip('#'), 1.0
  elif '+' in x:
    name, offset = tuple(x.split('+'))
    dtype = 'double' if '.' in offset else 'int'
    return name, '(%s)(%s)' % (dtype, offset)
  else:
    return x, '0'

def gen_mem(opcode, args, index, labels, names):
  return 'allocDelayMem("%s", %d);' % (args[0], int(args[1]))

def gen_eq(opcode, args, index, labels, names):
  if '+' in args[1]:
    names[args[0]] = args[1].split('+')
    return ''
  else:
    names[args[0]] = None
    if '.' in args[1]:
      return 'double %s = %s;' % (args[0], args[1])
    else:
      return 'int %s = %s;' % (args[0], args[1].upper())

def gen_skp(opcode, args, index, labels, names):
  if args[1].isdigit():
    offset = int(args[1])
  else:
    offset = labels[args[1]] - index - 1
  return 'skip(SKP_%s, %d);' % (args[0].upper(), offset)

def gen_wldr(opcode, args, index, labels, names):
  args[0] = args[0][-1]
  return 'loadRampLFO(%d, %d, %d);' % tuple(map(int, args))

def gen_wlds(opcode, args, index, labels, names):
  args[0] = args[0][-1]
  return 'loadSinLFO(%d, %d, %d);' % tuple(map(int, args))

def gen_rda(opcode, args, index, labels, names):
  address = args[0]
  if address.isdigit():
    return 'readDelay(%s, %s);' % (int(address), args[1])
  else:
    name, offset = name_offset(address, names)
    return 'readDelay("%s", %s, %s);' % (name, offset, args[1])

def gen_wrap(opcode, args, index, labels, names):
  address = args[0]
  if address.isdigit():
    return 'writeAllpass(%s, %s);' % (int(address), args[1])
  else:
    name, offset = name_offset(address, names)
    return 'writeAllpass("%s", %s, %s);' % (name, offset, args[1])

def gen_wrax(opcode, args, index, labels, names):
  register = args[0].lower() if args[0] in names else args[0].upper()
  return 'writeRegister(%s, %s);' % (register, args[1])

def gen_maxx(opcode, args, index, labels, names):
  register = args[0].lower() if args[0] in names else args[0].upper()
  return 'maxx(%s, %s);' % (register, args[1])

def gen_rdax(opcode, args, index, labels, names):
  register = args[0].lower() if args[0] in names else args[0].upper()
  return 'readRegister(%s, %s);' % (register, args[1])

def gen_ldax(opcode, args, index, labels, names):
  register = args[0].lower() if args[0] in names else args[0].upper()
  return 'loadAccumulator(%s);' % (register)

def gen_rdfx(opcode, args, index, labels, names):
  register = args[0].lower() if args[0] in names else args[0].upper()
  return 'readRegisterFilter(%s, %s);' % (register, args[1])

def gen_wra(opcode, args, index, labels, names):
  address = args[0]
  if address.isdigit():
    return 'writeDelay(%s, %s);' % (int(address), args[1])
  else:
    name, offset = name_offset(address, names)
    return 'writeDelay("%s", %s, %s);' % (name, offset, args[1])

def gen_sof(opcode, args, index, labels, names):
  return 'scaleOffset(%s, %s);' % (args[0], args[1])

def gen_log(opcode, args, index, labels, names):
  return 'log(%s, %s);' % (args[0], args[1])

def gen_exp(opcode, args, index, labels, names):
  return 'exp(%s, %s);' % (args[0], args[1])

def gen_mulx(opcode, args, index, labels, names):
  register = args[0].lower() if args[0] in names else args[0].upper()
  return 'mulx(%s);' % register

def gen_wrhx(opcode, args, index, labels, names):
  register = args[0].lower() if args[0] in names else args[0].upper()
  return 'writeRegisterHighshelf(%s, %s);' % (register, args[1])

def gen_wrlx(opcode, args, index, labels, names):
  register = args[0].lower() if args[0] in names else args[0].upper()
  return 'writeRegisterLowshelf(%s, %s);' % (register, args[1])

def gen_cho_rdal(opcode, args, index, labels, names):
  return 'chorusReadValue(CHO_LFO_%s);' % (args[0].upper())

def gen_cho_rda(opcode, args, index, labels, names):
  flags = chorus_flags(args[1])
  address = args[2]
  if address.isdigit():
    return 'chorusReadDelay(CHO_LFO_%s, %s, %d);' % (args[0].upper(), flags, int(address))
  else:
    name, offset = name_offset(address, names)
    return 'chorusReadDelay(CHO_LFO_%s, %s, "%s", %s);' % (args[0].upper(), flags, name, offset)

def gen_cho_sof(opcode, args, index, labels, names):
  flags = chorus_flags(args[1])
  return 'chorusScaleOffset(CHO_LFO_%s, %s, %s);' % (args[0].upper(), flags, args[2])

def gen_clr(opcode, args, index, labels, names):
  return 'clear();'

def gen_absa(opcode, args, index, labels, names):
  return 'absa();'

def gen_or(opcode, args, index, labels, names):
  value = '0x' + args[0][1:] if args[0].startswith('$') else args[0]
  return 'or(%s);' % value

def gen_and(opcode, args, index, labels, names):
  value = '0x' + args[0][1:] if args[0].startswith('$') else args[0]
  return 'and(%s);' % value

def gen_rmpa(opcode, args, index, labels, names):
  return 'readDelayPointer(%s);' % args[0]

OPCODES = {
  'absa': gen_absa,
  'mem': gen_mem,
  'equ': gen_eq,
  'skp': gen_skp,
  'wldr': gen_wldr,
  'rda': gen_rda,
  'rdax': gen_rdax,
  'ldax': gen_ldax,
  'wra': gen_wra,
  'wrax': gen_wrax,
  'sof': gen_sof,
  'rdfx': gen_rdfx,
  'cho.rda': gen_cho_rda,
  'cho.rdal': gen_cho_rdal,
  'cho.sof': gen_cho_sof,
  'wlds': gen_wlds,
  'wrap': gen_wrap,
  'mulx': gen_mulx,
  'wrhx': gen_wrhx,
  'wrlx': gen_wrlx,
  'log': gen_log,
  'exp': gen_exp,
  'clr': gen_clr,
  'or': gen_or,
  'and': gen_and,
  'rmpa': gen_rmpa,
  'maxx': gen_maxx
}

def parse(lines):
  label = None
  for line in lines:
    line = line.strip()
    line = line.split(';')[0]
    if not line:
      continue
    tokens = [x for x in re.split('[ \t,\r\n]+', line) if x]
    opcode = tokens[0].lower()
    args = tokens[1:]
    if opcode == 'cho':
      opcode = 'cho.%s' % args[0].lower()
      args = args[1:]
    if opcode.endswith(':'):
      label = opcode.rstrip(':')
      continue
    else:
      yield label, opcode, args
      label = None

def translate(program):
  # Collect label offsets
  labels = {}
  for index, (label, _, _) in enumerate(program):
    if label:
      labels[label] = index

  names = {}
  for index, (_, opcode, args) in enumerate(program):
    if opcode in OPCODES:
      yield OPCODES[opcode](opcode, args, index, labels, names)
    else:
      print >> sys.stderr, '>> CANNOT PARSE', (opcode, args)
      sys.exit(1)

def main(args):
  source = list(file(sys.argv[1]))
  java_code = translate(list(parse(source)))

  if len(sys.argv) >= 3:
    path, file_name = os.path.split(sys.argv[2])
    file_name, _ = os.path.splitext(file_name)
    class_name = file_name
    destination = file(sys.argv[2], 'w')
  else:
    path, file_name = os.path.split(sys.argv[1])
    file_name, _ = os.path.splitext(file_name)
    class_name = ''.join(token.title() for token in file_name.split('_'))
    destination = sys.stdout
  for line in output(java_code, class_name, source):
    destination.write(line + '\n')


if __name__ == '__main__':
  main(sys.argv[1:])
