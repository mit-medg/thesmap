This directory is to the prototype of LVG rule trie.

--------------
I. Data file preparation: old format to new format

1. All original data are under:
/aux/lu/References/Java/TriePersistent/data.org (with some rule missing "$").

2. All corrected original data are under:
/aux/lu/References/Java/TriePersistent/data.old
=> Add a "#" to the beginning of all blank lines
=> All rule should include "$"
=> Remove duplicated rules:
  - In dm.rul:
  - Warning: duplicate rule (ant$|adj|base|ance$|noun|base)
  - Warning: duplicate rule (ance$|noun|base|ant$|adj|base)

  - Warning: duplicate rule (ent$|adj|base|ency$|noun|base)
  - Warning: duplicate rule (ency$|noun|base|ent$|adj|base)

  - Warning: duplicate rule (ious$|adj|base|ion$|noun|base)
  - Warning: duplicate rule (ion$|noun|base|ious$|adj|base)

  - Warning: duplicate rule (istic$|adj|base|ism$|noun|base)
  - Warning: duplicate rule (ism$|noun|base|istic$|adj|base)


3. Modify file to a standard format
* Inflection
Use "ModifyFile" to change from old format to new format and put new files 
under ./data for inflection and derivation rules or 
../../data/rules/

=> java ModifyFile ./data.old/im.rul ./data/im.rul
=> java ModifyFile ./data.old/plural.rul ./data/plural.rul
=> java ModifyFile ./data.old/verbinfl.rul ./data/verbinfl.rul

* Derivation
Use "ModifyFileD" to change from old format to new format and put new files
under ./data for derivation rules
=> java ModifyFileD ./data.old/dm.rul ./data/dm.rul

* difference between old and new file format:
1. Add key wrods: RULE, EXCEPTION, and FILE
2. Change WildCard to new Set of {V, C, L, D, $, ^)
3. Unify inflections to new set defined in Lvg.
	=> ing => presPart
	=> present => pres
	=> pastpart => pastPart

--------------
II. Run trie from RAM.
=> java RamTrie <term> <-i/-d> <-ps> 

=> Get inflection and derivation from Ram.

--------------
III. Generating Persistent files:
- ./PersistentData/trie.data
- ./PersistentData/rule.data
- ./PersistentData/exception.data

=> rm *.data
=> java PersistentTrieTree <-i/d>

=> Build persistent file for inflection or derivation

=> move *.data to ../../data/rules/.
=> Inflection: trieI.data, ruleI.data, exceptionI.data
=> Derivation: trieD.data, ruleD.data, exceptionD.data

--------------
IV. Run trie from Persistent files
=> java PersistentTrie <term> <-i/d> <-s>

=> Get inflection, uninflection, and derivation
=> -s is not function.
