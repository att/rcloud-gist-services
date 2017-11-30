package com.mangosolutions.rcloud.rawgist.repository.git;

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mangosolutions.rcloud.rawgist.model.FileContent;

class RCloudFilenameComparator implements Comparator<FileContent>, Serializable {

    private static Pattern RCLOUD_PART_PATTERN = Pattern.compile("part(\\d+).*"); 
    
    private static final long serialVersionUID = 6590929523448823566L;

    @Override
    public int compare(FileContent o1, FileContent o2) {
        String name1 = StringUtils.trimToEmpty(o1.getFilename());
        String name2 = StringUtils.trimToEmpty(o2.getFilename());
        int result = 0;
        Matcher name1Matcher = RCLOUD_PART_PATTERN.matcher(name1);
        Matcher name2Matcher = RCLOUD_PART_PATTERN.matcher(name2);
        if(name1Matcher.matches() && name2Matcher.matches()) {
            result = sortParts(name1Matcher, name2Matcher);
        } else if(name1Matcher.matches()) {
            result = -1;
        } else if(name2Matcher.matches()) {
            result = 1;
        } else {
            result = StringUtils.compareIgnoreCase(name1, name2);
        }
        return result;
    }

    private int sortParts(Matcher name1Matcher, Matcher name2Matcher) {
        String numberString1 = name1Matcher.group(1);
        String numberString2 = name2Matcher.group(1);
        int number1 = Integer.valueOf(numberString1);
        int number2 = Integer.valueOf(numberString2);
        return number1 - number2;
    }

}
