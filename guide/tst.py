def calculate_mb(choices):

    new_choices = []
    for i in range(1,8):
        new_choices.append([int(choices[j-1]) for j in range(i,71,7) ])

    print (new_choices)
        
    res = list("XXXX")

    ei = sum(new_choices[0])
    if ei < 0: res[0] = 'E'
    else: res[0] = 'I'
    
    sn = sum(new_choices[1]) + sum(new_choices[2])
    if sn < 0: res[1] = 'S'
    else: res[1] = 'N'
    
    tf = sum(new_choices[3]) + sum(new_choices[4])
    if tf < 0: res[2] = 'T'
    else: res[2] = 'F'

    jp = sum(new_choices[5]) + sum(new_choices[6])
    if jp < 0: res[3] = 'J'
    else: res[3] = 'P'
    
    return str(''.join(res))

input = [1, -1, -1, 1, 0, 0, 0,
         1, 0, -1, 0, 0, -1, -1,
         1, -1, 0, 0, 1, 1, -1,
         1, 1, 1, 0, 0, 0, -1,
         1, 0, 1, 1, 0, 0,0,
         0, 0, 1, -1, -1, 1,1,
         1, 1, 1, 1, -1, 1,1,
         1, 0, -1, -1, -1, -1,1,
         0, 0, 0, 0, -1, 0,0,
         -1, 0, -1, -1, -1, 0, 0]

print (calculate_mb(input))


