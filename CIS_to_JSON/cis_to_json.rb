# Author: Olli Aro
# Email: olli_aro@yahoo.co.uk
# Date: 07/02/2013
# License: This code is available under GNU General Public License (http://www.gnu.org/licenses/gpl.html)
#
# This script converts files from ATCO CIF format to JSON. ATCO CIF is a file standard used for for bus stop and timetable
# information in the UK.
#
# Usage:
# 1) Install the ATCO-CIF parser gem from https://github.com/davidjrice/atco
# 2) Put your source CIF files in the /cif/ directory.
# 3) Run the script and the JSON files are saved in /json/ directory.

require 'rubygems'
require 'atco'

if __FILE__ == $0
  path = Dir.pwd
  sources = Dir.glob(path+"/cif/*")
  for source in sources
    result = Atco.parse(source)
    filename = source[(source.index("/cif/")+5)..(source.index(".")-1)] 
    filename = path+"/json/"+filename+".json"
    File.open(filename, 'w') { |file| file.write(result) }
    print "Created file:#{filename}\n"
  end
end