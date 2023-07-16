
planets = ['sun','mo','mer','ven','mar','ju','sa','ur','ne','pl']

def init():
    import pandas as pd
    mapping = pd.DataFrame(index=planets,columns=['tick','*','sq','tri','opp'])
    mapping.loc['mo']['tick'] = {'sun':245,'mer':145,'ven':146,'mar':147,'ju':148,'sa':149,'ur':150,'ne':151,'pl':254}
    mapping.loc['mo']['tri'] = {'sun':246,'mer':152,'ven':153,'mar':154,'ju':155,'sa':156,'ur':157,'ne':158,'pl':255}
    mapping.loc['mo']['*'] = {'sun':246,'mer':152,'ven':153,'mar':154,'ju':155,'sa':156,'ur':157,'ne':158,'pl':255}
    mapping.loc['mo']['sq'] = {'sun':247,'mer':159,'ven':160,'mar':161,'ju':162,'sa':163,'ur':164,'ne':165,'pl':256}
    mapping.loc['mo']['opp'] = {'sun':247,'mer':159,'ven':160,'mar':161,'ju':162,'sa':163,'ur':164,'ne':165,'pl':256}
    mapping.loc['ur']['tick'] = {'ne':242,'pl':272}
    mapping.loc['ur']['tri'] = {'ne':243,'pl':273}
    mapping.loc['ur']['*'] = {'ne':243,'pl':273}
    mapping.loc['ur']['sq'] = {'ne':244,'pl':274}
    mapping.loc['ur']['opp'] = {'ne':244,'pl':274}
    mapping.loc['sun']['tick'] = {'mer':166,'ven':167,'mar':168,'ju':169,'sa':170,'ur':171,'ne':172,'pl':251}
    mapping.loc['sun']['tri'] = {'ven':248,'mar':173,'ju':174,'sa':175,'ur':176,'ne':177,'pl':252}
    mapping.loc['sun']['*'] = {'ven':248,'mar':173,'ju':174,'sa':175,'ur':176,'ne':177,'pl':252}
    mapping.loc['sun']['sq'] = {'mar':178,'ju':179,'sa':180,'ur':181,'ne':182,'pl':253}
    mapping.loc['sun']['opp'] = {'mar':178,'ju':179,'sa':180,'ur':181,'ne':182,'pl':253}
    mapping.loc['sa']['tick'] = {'ur':236,'ne':237,'pl':269}
    mapping.loc['sa']['tri'] = {'ur':238,'ne':239,'pl':270}
    mapping.loc['sa']['*'] = {'ur':238,'ne':239,'pl':270}
    mapping.loc['sa']['sq'] = {'ur':240,'ne':241,'pl':271}
    mapping.loc['sa']['opp'] = {'ur':240,'ne':241,'pl':271}
    mapping.loc['mer']['tick'] = {'ven':183,'mar':184,'ju':185,'sa':186,'ur':187,'ne':188,'pl':257}
    mapping.loc['mer']['tri'] = {'ven':189,'mar':190,'ju':191,'sa':192,'ur':193,'ne':194,'pl':258}
    mapping.loc['mer']['*'] = {'ven':189,'mar':190,'ju':191,'sa':192,'ur':193,'ne':194,'pl':258}
    mapping.loc['mer']['sq'] = {'mar':195,'ju':196,'sa':197,'ur':198,'ne':199,'pl':259}
    mapping.loc['mer']['opp'] = {'mar':195,'ju':196,'sa':197,'ur':198,'ne':199,'pl':259}
    mapping.loc['ju']['tick'] = {'sa':227,'ur':228,'ne':229,'pl':266}
    mapping.loc['ju']['tri'] = {'sa':230,'ur':231,'ne':232,'pl':267}
    mapping.loc['ju']['*'] = {'sa':230,'ur':231,'ne':232,'pl':267}
    mapping.loc['ju']['sq'] = {'sa':233,'ur':234,'ne':235,'pl':268}
    mapping.loc['ju']['opp'] = {'sa':233,'ur':234,'ne':235,'pl':268}
    mapping.loc['ven']['tick'] = {'mar':200,'ju':201,'sa':202,'ur':203,'ne':204,'pl':260}
    mapping.loc['ven']['tri'] = {'mar':205,'ju':206,'sa':207,'ur':208,'ne':209,'pl':261}
    mapping.loc['ven']['*'] = {'mar':205,'ju':206,'sa':207,'ur':208,'ne':209,'pl':261}
    mapping.loc['ven']['sq'] = {'mar':210,'ju':211,'sa':212,'ur':213,'ne':214,'pl':262}
    mapping.loc['ven']['opp'] = {'mar':210,'ju':211,'sa':212,'ur':213,'ne':214,'pl':262}
    mapping.loc['mar']['tick'] = {'ju':215,'sa':216,'ur':217,'ne':218,'pl':263}
    mapping.loc['mar']['tri'] = {'ju':219,'sa':220,'ur':221,'ne':222,'pl':264}
    mapping.loc['mar']['*'] = {'ju':219,'sa':220,'ur':221,'ne':222,'pl':264}
    mapping.loc['mar']['sq'] = {'ju':223,'sa':224,'ur':225,'ne':226,'pl':265}
    mapping.loc['mar']['opp'] = {'ju':223,'sa':224,'ur':225,'ne':226,'pl':265}
    mapping.loc['ne']['tick'] = {'pl':275}
    mapping.loc['ne']['tri'] = {'pl':276}
    mapping.loc['ne']['*'] = {'pl':276}
    mapping.loc['ne']['sq'] = {'pl':277}
    mapping.loc['ne']['opp'] = {'pl':277}    
    return mapping

mbti = {'estp':['se','ti','fe','ni'],'esfp':['se','fi','te','ni'],
        'istj': ['si','te','fi','ne'],'isfj':['si','fe','ti','ne'],
        'entp': ['ne','ti','fe','si'], 'enfp': ['ne','fi','te','si'],
        'intj': ['ni','te','fi','se'], 'infj': ['ni','fe','ti','se'],
        'estj': ['te','si','ne','fi'], 'entj': ['te','ni','se','fi'],
        'istp': ['ti','se','ni','fe'], 'intp': ['ti','ne','si','fe'],
        'esfj': ['fe','si','ne','ti'], 'enfj': ['fe','ni','se','ti'],
        'isfp': ['fi','se','ni','te'], 'infp': ['fi','ne','si','te']}
