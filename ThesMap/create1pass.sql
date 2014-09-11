create table NSTR2INFO
select cns.nstr, cns.cui, cns.str, s.tui, s.sty
from (select distinct ns.nstr, ns.cui, c.str from MRXNS_ENG as ns join 
     (select cx.cui, cx.str from MRCONSO cx 
     	     where  cx.ts='P' and cx.stt='PF' and cx.ispref='Y' 
	     	    and cx.cvf is not null and cx.LAT='ENG') as c
	on ns.cui=c.cui) as cns
     join MRSTY as s on cns.cui=s.cui
;

create index x_nstr2info on NSTR2INFO(nstr(255));
