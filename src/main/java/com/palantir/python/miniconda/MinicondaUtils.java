/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.python.miniconda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jjuelich on 3/6/17.
 */
public final class MinicondaUtils {

    public static List<String> convertChannelsToArgs(List<String> channels) {
        List<String> args = new ArrayList<>();
        for (String channel : channels) {
            args.add("--channel");
            args.add(channel);
        }
        return Collections.unmodifiableList(args);
    }

    private MinicondaUtils() {}
}
